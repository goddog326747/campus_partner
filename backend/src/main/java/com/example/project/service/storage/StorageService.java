package com.example.project.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 存储服务接口 - 文件上传下载的统一抽象
 * <p>
 * 【设计模式】策略模式（Strategy Pattern）
 * - 定义统一的存储接口，支持多种存储后端（本地、OSS、云存储等）
 * - 通过配置切换存储策略，业务代码无需改动
 * </p>
 * <p>
 * 【使用场景】
 * 1. 用户头像上传
 * 2. 帖子图片上传
 * 3. 文件附件存储
 * </p>
 * <p>
 * 【配置方式】在 application.yml 中配置：
 * <pre>
 * storage:
 *   type: local  # 可选：local（本地）或 oss（阿里云OSS）
 *   local:
 *     base-path: C:/Users/void/Downloads/project/img
 *     url-prefix: /img
 *   max-file-size: 10485760  # 10MB
 *   allowed-extensions: jpg, jpeg, png, gif
 * </pre>
 *
 * @author system
 * @since 1.0
 */
public interface StorageService {

    /**
     * 上传文件到默认目录
     * <p>
     * 【自动处理】
     * - 生成唯一文件名（UUID）
     * - 按日期分目录存储（yyyy/MM/dd）
     * - 返回完整访问URL
     * </p>
     *
     * @param file 要上传的文件（Spring 封装的 MultipartFile）
     * @return 上传成功后的文件完整URL，如：/img/2024/01/15/abc123.jpg
     * @throws IllegalArgumentException 文件为空或格式不合法时抛出
     * @throws RuntimeException         上传失败时抛出
     * @example
     * <pre>
     * String avatarUrl = storageService.upload(multipartFile);
     * // 返回：/img/2024/01/15/a1b2c3d4e5f6.jpg
     * </pre>
     */
    String upload(MultipartFile file);

    /**
     * 上传文件到指定目录
     * <p>
     * 【适用场景】需要按业务分类存储文件
     * </p>
     *
     * @param file      要上传的文件
     * @param directory 目标目录名称，如："avatar"（头像）、"post"（帖子图片）
     * @return 上传成功后的文件完整URL
     * @example
     * <pre>
     * // 上传用户头像到 avatar 目录
     * String url = storageService.upload(file, "avatar");
     * // 返回：/img/avatar/2024/01/15/abc123.jpg
     * </pre>
     */
    String upload(MultipartFile file, String directory);

    /**
     * 通过输入流上传文件（用于程序生成的文件）
     * <p>
     * 【适用场景】
     * - 生成验证码图片后上传
     * - 导出Excel后上传
     * - 其他非用户上传的文件
     * </p>
     *
     * @param inputStream 文件输入流（调用方负责关闭）
     * @param fileName    原始文件名（用于获取扩展名）
     * @param contentType MIME类型，如：image/jpeg、application/pdf
     * @return 上传成功后的文件完整URL
     */
    String upload(InputStream inputStream, String fileName, String contentType);

    /**
     * 通过输入流上传文件到指定目录
     *
     * @param inputStream 文件输入流
     * @param fileName    原始文件名
     * @param contentType MIME类型
     * @param directory   目标目录名称
     * @return 上传成功后的文件完整URL
     */
    String upload(InputStream inputStream, String fileName, String contentType, String directory);

    /**
     * 下载文件
     * <p>
     * 【使用方式】配合 HTTP 响应输出给客户端
     * </p>
     *
     * @param fileUrl 文件URL（即 upload 方法返回的URL）
     * @return 文件字节数组
     * @throws RuntimeException 文件不存在或读取失败时抛出
     * @example
     * <pre>
     * byte[] bytes = storageService.download("/img/2024/01/15/abc123.jpg");
     * response.getOutputStream().write(bytes);
     * </pre>
     */
    byte[] download(String fileUrl);

    /**
     * 删除文件
     * <p>
     * 【注意】删除后无法恢复，请谨慎使用
     * </p>
     *
     * @param fileUrl 要删除的文件URL
     */
    void delete(String fileUrl);

    /**
     * 检查文件是否存在
     *
     * @param fileUrl 文件URL
     * @return true-存在，false-不存在
     */
    boolean exists(String fileUrl);

    /**
     * 获取文件的完整URL
     * <p>
     * 【用途】将相对路径转换为完整访问URL
     * </p>
     *
     * @param relativePath 文件相对路径，如：2024/01/15/abc123.jpg
     * @return 完整的文件URL，如：/img/2024/01/15/abc123.jpg
     */
    String getFullUrl(String relativePath);
}
