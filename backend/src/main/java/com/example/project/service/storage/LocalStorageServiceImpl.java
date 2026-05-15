package com.example.project.service.storage;

import com.example.project.service.storage.StorageProperties.Local;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地存储服务实现 - 文件存储在服务器本地磁盘
 * <p>
 * 【实现说明】
 * 本类实现了 StorageService 接口，将文件存储在应用服务器的本地文件系统中。
 * 适合单机部署、开发测试环境，或文件量不大的场景。
 * </p>
 * <p>
 * 【存储路径结构】
 * <pre>
 * 配置 base-path: C:/Users/void/Downloads/project/img
 * 实际存储: C:/Users/void/Downloads/project/img/avatar/2024/01/15/abc123.jpg
 *                              │      │   │  │  │
 *                              │      │   │  │  └─ 文件名（UUID）
 *                              │      │   │  └──── 日
 *                              │      │   └─────── 月
 *                              │      └─────────── 年
 *                              └────────────────── 业务目录（avatar/post等）
 * </pre>
 * </p>
 * <p>
 * 【访问方式】
 * - 存储路径：C:/Users/void/Downloads/project/img/avatar/2024/01/15/abc123.jpg
 * - 访问URL：http://localhost:8080/img/avatar/2024/01/15/abc123.jpg
 * - 由 Spring Boot 的静态资源映射处理 /img/** 请求
 * </p>
 * <p>
 * 【优缺点】
 * 优点：
 * - 实现简单，无需额外依赖
 * - 本地访问速度快
 * - 不依赖第三方服务
 * <p>
 * 缺点：
 * - 不适合集群部署（多台服务器文件不同步）
 * - 服务器磁盘空间有限
 * - 没有CDN加速
 * </p>
 *
 * @author system
 * @see StorageService
 * @since 1.0
 */
@Slf4j
@Service("localStorageService")
@Primary  // 设置为默认的 StorageService 实现
public class LocalStorageServiceImpl implements StorageService {

    /**
     * 本地存储配置，从 application.yml 的 storage.local 读取
     * - basePath: 文件存储的物理路径
     * - urlPrefix: 访问文件的URL前缀
     */
    private final Local localConfig;

    /**
     * 存储全局配置，包含文件大小限制、允许的文件类型等
     */
    private final StorageProperties storageProperties;

    /**
     * 构造方法 - 初始化时自动创建存储目录
     *
     * @param storageProperties 存储配置属性
     */
    public LocalStorageServiceImpl(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.localConfig = storageProperties.getLocal();
        init();
    }

    /**
     * 初始化存储目录
     * <p>
     * 应用启动时自动执行，确保存储目录存在。
     * 如果目录不存在则创建，创建失败则抛出异常阻止应用启动。
     * </p>
     */
    private void init() {
        Path basePath = Paths.get(localConfig.getBasePath());
        try {
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("【存储初始化】创建存储目录成功: {}", basePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("【存储初始化失败】无法创建存储目录: " + basePath, e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * 【本地存储实现】
     * 1. 校验文件合法性（类型、大小）
     * 2. 生成唯一文件名：UUID + 原扩展名
     * 3. 构建存储路径：basePath/yyyy/MM/dd/filename
     * 4. 复制文件到目标位置
     * 5. 返回可访问的URL
     * </p>
     */
    @Override
    public String upload(MultipartFile file) {
        return upload(file, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 【本地存储实现】
     * 如果指定了 directory，文件会存储在：basePath/directory/yyyy/MM/dd/filename
     * 便于按业务类型分类管理文件
     * </p>
     *
     * @param file      要上传的文件
     * @param directory 业务目录，如 "avatar"、"post"、"comment"
     */
    @Override
    public String upload(MultipartFile file, String directory) {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("【上传失败】文件不能为空");
        }

        // 2. 获取文件信息
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // 3. 校验文件类型和大小
        validateFile(extension, file.getSize());

        // 4. 生成唯一文件名和存储路径
        String fileName = generateFileName(extension);
        String relativePath = buildRelativePath(fileName, directory);

        // 5. 执行文件存储
        try {
            Path targetPath = Paths.get(localConfig.getBasePath(), relativePath);
            // 创建父目录（如果不存在）
            Files.createDirectories(targetPath.getParent());
            // 复制文件（REPLACE_EXISTING 表示覆盖已存在的文件）
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("【文件上传成功】原始文件名: {}, 存储路径: {}", originalFilename, targetPath);
        } catch (IOException e) {
            log.error("【文件上传失败】原始文件名: {}", originalFilename, e);
            throw new RuntimeException("文件上传失败: " + originalFilename, e);
        }

        // 6. 返回完整URL
        return getFullUrl(relativePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        return upload(inputStream, fileName, contentType, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 【适用场景】处理 Base64 图片、程序生成的文件等
     * </p>
     */
    @Override
    public String upload(InputStream inputStream, String fileName, String contentType, String directory) {
        if (inputStream == null) {
            throw new IllegalArgumentException("【上传失败】输入流不能为空");
        }

        String extension = getFileExtension(fileName);
        validateExtension(extension);

        String newFileName = generateFileName(extension);
        String relativePath = buildRelativePath(newFileName, directory);

        try {
            Path targetPath = Paths.get(localConfig.getBasePath(), relativePath);
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("【文件上传成功】文件名: {}, 存储路径: {}", fileName, targetPath);
        } catch (IOException e) {
            log.error("【文件上传失败】文件名: {}", fileName, e);
            throw new RuntimeException("文件上传失败: " + fileName, e);
        }

        return getFullUrl(relativePath);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 【本地存储实现】从本地磁盘读取文件字节
     * </p>
     */
    @Override
    public byte[] download(String fileUrl) {
        String relativePath = extractRelativePath(fileUrl);
        Path filePath = Paths.get(localConfig.getBasePath(), relativePath);

        try {
            if (!Files.exists(filePath)) {
                throw new RuntimeException("【下载失败】文件不存在: " + fileUrl);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("【下载失败】文件: {}", fileUrl, e);
            throw new RuntimeException("文件下载失败: " + fileUrl, e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * 【本地存储实现】从本地磁盘删除文件
     * </p>
     */
    @Override
    public void delete(String fileUrl) {
        String relativePath = extractRelativePath(fileUrl);
        Path filePath = Paths.get(localConfig.getBasePath(), relativePath);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("【文件删除成功】路径: {}", filePath);
            }
        } catch (IOException e) {
            log.error("【文件删除失败】URL: {}", fileUrl, e);
            throw new RuntimeException("文件删除失败: " + fileUrl, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String fileUrl) {
        String relativePath = extractRelativePath(fileUrl);
        Path filePath = Paths.get(localConfig.getBasePath(), relativePath);
        return Files.exists(filePath);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 【本地存储实现】拼接 urlPrefix 和相对路径
     * </p>
     *
     * @param relativePath 相对路径，如：avatar/2024/01/15/abc123.jpg
     * @return 完整URL，如：/img/avatar/2024/01/15/abc123.jpg
     */
    @Override
    public String getFullUrl(String relativePath) {
        return localConfig.getUrlPrefix() + "/" + relativePath.replace("\\", "/");
    }

    /**
     * 从完整URL中提取相对路径
     * <p>
     * 例如：/img/avatar/2024/01/15/abc123.jpg → avatar/2024/01/15/abc123.jpg
     * </p>
     *
     * @param fileUrl 完整URL
     * @return 相对路径
     */
    private String extractRelativePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("【路径解析失败】文件URL不能为空");
        }

        String urlPrefix = localConfig.getUrlPrefix();
        if (fileUrl.startsWith(urlPrefix)) {
            return fileUrl.substring(urlPrefix.length() + 1);
        }
        return fileUrl;
    }

    /**
     * 生成唯一文件名
     * <p>
     * 使用 UUID 生成32位随机字符串，避免文件名冲突
     * </p>
     *
     * @param extension 文件扩展名，如：jpg、png
     * @return 新文件名，如：a1b2c3d4e5f678901234567890123456.jpg
     */
    private String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    /**
     * 构建相对存储路径
     * <p>
     * 按日期分目录存储，便于管理和清理：
     * - 无目录：2024/01/15/filename.jpg
     * - 有目录：avatar/2024/01/15/filename.jpg
     * </p>
     *
     * @param fileName  文件名
     * @param directory 业务目录（可选）
     * @return 相对路径
     */
    private String buildRelativePath(String fileName, String directory) {
        // 按日期分目录：年/月/日
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        if (directory != null && !directory.isEmpty()) {
            return directory + "/" + datePath + "/" + fileName;
        }
        return datePath + "/" + fileName;
    }

    /**
     * 获取文件扩展名
     * <p>
     * 从文件名中提取扩展名，如果没有则默认返回 jpg
     * </p>
     *
     * @param fileName 文件名，如：photo.jpg
     * @return 扩展名，如：jpg
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "jpg";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg";
    }

    /**
     * 校验文件（类型 + 大小）
     *
     * @param extension 文件扩展名
     * @param fileSize  文件大小（字节）
     */
    private void validateFile(String extension, long fileSize) {
        validateExtension(extension);
        validateFileSize(fileSize);
    }

    /**
     * 校验文件扩展名是否在允许列表中
     *
     * @param extension 文件扩展名
     * @throws IllegalArgumentException 扩展名不合法时抛出
     */
    private void validateExtension(String extension) {
        if (!storageProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("【上传失败】不支持的文件类型: " + extension);
        }
    }

    /**
     * 校验文件大小是否超过限制
     *
     * @param fileSize 文件大小（字节）
     * @throws IllegalArgumentException 文件过大时抛出
     */
    private void validateFileSize(long fileSize) {
        if (fileSize > storageProperties.getMaxFileSize()) {
            long maxSizeMB = storageProperties.getMaxFileSize() / 1024 / 1024;
            throw new IllegalArgumentException("【上传失败】文件大小超过限制，最大允许: " + maxSizeMB + "MB");
        }
    }
}
