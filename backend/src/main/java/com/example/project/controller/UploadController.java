package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.service.storage.StorageService;
import com.example.project.service.storage.StorageServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 * <p>
 * 提供文件上传和删除的API接口，支持单文件和多文件上传，支持指定目录上传
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final StorageServiceFactory storageServiceFactory;

    public UploadController(StorageServiceFactory storageServiceFactory) {
        this.storageServiceFactory = storageServiceFactory;
    }

    /**
     * 上传单个图片文件
     *
     * @param file 要上传的图片文件
     * @return 上传结果，包含文件URL、原始文件名和文件大小
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            String url = storageService.upload(file);

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("originalName", file.getOriginalFilename());
            result.put("size", String.valueOf(file.getSize()));

            log.info("Image uploaded successfully: {}", url);
            return Result.success("Upload successful", result);
        } catch (IllegalArgumentException e) {
            log.warn("Upload failed: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("Upload failed", e);
            return Result.error(500, "Upload failed: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片文件
     *
     * @param files 要上传的图片文件数组
     * @return 上传结果列表，每个元素包含文件URL、原始文件名和文件大小
     */
    @PostMapping("/images")
    public Result<List<Map<String, String>>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            List<Map<String, String>> results = new ArrayList<>();

            for (MultipartFile file : files) {
                String url = storageService.upload(file);

                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("url", url);
                fileInfo.put("originalName", file.getOriginalFilename());
                fileInfo.put("size", String.valueOf(file.getSize()));
                results.add(fileInfo);
            }

            log.info("Images uploaded successfully: {} files", files.length);
            return Result.success("Upload successful", results);
        } catch (IllegalArgumentException e) {
            log.warn("Upload failed: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("Upload failed", e);
            return Result.error(500, "Upload failed: " + e.getMessage());
        }
    }

    /**
     * 上传单个图片文件到指定目录
     *
     * @param file      要上传的图片文件
     * @param directory 目标目录名称
     * @return 上传结果，包含文件URL、原始文件名和文件大小
     */
    @PostMapping("/image/{directory}")
    public Result<Map<String, String>> uploadImageToDirectory(
            @RequestParam("file") MultipartFile file,
            @PathVariable("directory") String directory) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            String url = storageService.upload(file, directory);

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("originalName", file.getOriginalFilename());
            result.put("size", String.valueOf(file.getSize()));

            log.info("Image uploaded successfully to directory {}: {}", directory, url);
            return Result.success("Upload successful", result);
        } catch (IllegalArgumentException e) {
            log.warn("Upload failed: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("Upload failed", e);
            return Result.error(500, "Upload failed: " + e.getMessage());
        }
    }

    /**
     * 删除指定URL的图片文件
     *
     * @param url 要删除的图片文件URL
     * @return 操作结果
     */
    @DeleteMapping("/image")
    public Result<Void> deleteImage(@RequestParam("url") String url) {
        try {
            StorageService storageService = storageServiceFactory.getStorageService();
            storageService.delete(url);

            log.info("Image deleted successfully: {}", url);
            return Result.success("Delete successful", null);
        } catch (Exception e) {
            log.error("Delete failed", e);
            return Result.error(500, "Delete failed: " + e.getMessage());
        }
    }

    /**
     * 获取当前存储服务类型信息
     *
     * @return 存储类型信息
     */
    @GetMapping("/storage-type")
    public Result<Map<String, String>> getStorageType() {
        Map<String, String> info = new HashMap<>();
        info.put("type", storageServiceFactory.getCurrentStorageType());
        return Result.success(info);
    }
}
