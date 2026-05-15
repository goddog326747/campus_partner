package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.service.storage.StorageService;
import com.example.project.shiro.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * 文件上传下载控制器
 * <p>
 * 【功能说明】
 * 提供统一的文件上传、下载接口，支持本地存储和OSS存储两种模式。
 * 通过配置 storage.type 切换存储后端，业务代码无需改动。
 * </p>
 * <p>
 * 【配置方式】application.yml
 * <pre>
 * storage:
 *   type: local  # local 或 oss
 *   local:
 *     base-path: C:/Users/void/Downloads/project/img
 *     url-prefix: /img
 *   max-file-size: 10485760  # 10MB
 *   allowed-extensions: jpg, jpeg, png, gif
 * </pre>
 * </p>
 * <p>
 * 【使用示例】
 * <pre>
 * // 1. 上传头像
 * POST /api/file/upload/avatar
 * Content-Type: multipart/form-data
 * file: [二进制图片文件]
 * 
 * // 2. 上传帖子图片
 * POST /api/file/upload/post
 * Content-Type: multipart/form-data
 * file: [二进制图片文件]
 * 
 * // 3. 下载文件
 * GET /api/file/download?url=/img/avatar/2024/01/15/abc123.jpg
 * </pre>
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    /**
     * 存储服务 - 自动注入默认实现（LocalStorageServiceImpl）
     * <p>
     * 通过 @Primary 注解，Spring 会自动选择 LocalStorageServiceImpl
     * 如需切换到 OSS，修改配置 storage.type=oss 即可
     * </p>
     */
    private final StorageService storageService;

    /**
     * 上传用户头像
     * <p>
     * 【接口说明】
     * - 存储路径：avatar/2024/01/15/uuid.jpg
     * - 文件限制：最大10MB，只允许图片格式
     * - 返回URL：/img/avatar/2024/01/15/uuid.jpg
     * </p>
     *
     * @param file 头像图片文件（multipart/form-data）
     * @return 上传成功后的文件URL
     * @apiNote 前端使用 FormData 提交：
     * <pre>
     * const formData = new FormData();
     * formData.append('file', fileInput.files[0]);
     * fetch('/api/file/upload/avatar', {method: 'POST', body: formData});
     * </pre>
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 检查用户是否登录
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        try {
            // 上传到 avatar 目录
            String fileUrl = storageService.upload(file, "avatar");
            log.info("【头像上传成功】用户ID: {}, 文件URL: {}", userId, fileUrl);
            return Result.success("上传成功", fileUrl);
        } catch (IllegalArgumentException e) {
            log.warn("【头像上传失败】用户ID: {}, 错误: {}", userId, e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("【头像上传失败】用户ID: {}", userId, e);
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传帖子图片
     * <p>
     * 【接口说明】
     * - 存储路径：post/2024/01/15/uuid.jpg
     * - 用于帖子内容中的图片
     * </p>
     *
     * @param file 图片文件
     * @return 上传成功后的文件URL
     */
    @PostMapping("/upload/post")
    public Result<String> uploadPostImage(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        try {
            String fileUrl = storageService.upload(file, "post");
            log.info("【帖子图片上传成功】用户ID: {}, 文件URL: {}", userId, fileUrl);
            return Result.success("上传成功", fileUrl);
        } catch (IllegalArgumentException e) {
            log.warn("【帖子图片上传失败】用户ID: {}, 错误: {}", userId, e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("【帖子图片上传失败】用户ID: {}", userId, e);
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 通用文件上传
     * <p>
     * 【接口说明】
     * - 可以指定存储目录
     * - 不指定目录则按日期存储
     * </p>
     *
     * @param file      文件
     * @param directory 目标目录（可选），如：comment、attachment
     * @return 上传成功后的文件URL
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", required = false) String directory) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        try {
            String fileUrl = storageService.upload(file, directory);
            log.info("【文件上传成功】用户ID: {}, 目录: {}, 文件URL: {}", userId, directory, fileUrl);
            return Result.success("上传成功", fileUrl);
        } catch (IllegalArgumentException e) {
            log.warn("【文件上传失败】用户ID: {}, 错误: {}", userId, e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("【文件上传失败】用户ID: {}", userId, e);
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * <p>
     * 【接口说明】
     * - 通过文件URL下载文件内容
     * - 返回二进制数据，前端可直接显示或下载
     * </p>
     *
     * @param url      文件URL（即上传接口返回的URL）
     * @param response HTTP响应对象，用于输出文件内容
     * @apiNote 前端使用：
     * <pre>
     * // 直接显示图片
     * &lt;img src="/api/file/download?url=/img/avatar/xxx.jpg" /&gt;
     * 
     * // 下载文件
     * window.open('/api/file/download?url=/img/xxx.jpg');
     * </pre>
     */
    @GetMapping("/download")
    public void downloadFile(@RequestParam("url") String url, HttpServletResponse response) {
        try {
            byte[] fileData = storageService.download(url);

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"download\"");
            response.setContentLength(fileData.length);

            // 输出文件内容
            try (OutputStream out = response.getOutputStream()) {
                out.write(fileData);
                out.flush();
            }

            log.debug("【文件下载成功】URL: {}", url);
        } catch (Exception e) {
            log.error("【文件下载失败】URL: {}", url, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 删除文件
     * <p>
     * 【接口说明】
     * - 根据文件URL删除存储的文件
     * - 删除后无法恢复，请谨慎使用
     * </p>
     *
     * @param url 文件URL
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteFile(@RequestParam("url") String url) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        try {
            storageService.delete(url);
            log.info("【文件删除成功】用户ID: {}, URL: {}", userId, url);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("【文件删除失败】用户ID: {}, URL: {}", userId, url, e);
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }
}
