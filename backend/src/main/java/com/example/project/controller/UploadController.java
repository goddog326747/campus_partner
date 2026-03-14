package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.storage.StorageService;
import com.example.project.storage.StorageServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final StorageServiceFactory storageServiceFactory;

    public UploadController(StorageServiceFactory storageServiceFactory) {
        this.storageServiceFactory = storageServiceFactory;
    }

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

    @GetMapping("/storage-type")
    public Result<Map<String, String>> getStorageType() {
        Map<String, String> info = new HashMap<>();
        info.put("type", storageServiceFactory.getCurrentStorageType());
        return Result.success(info);
    }
}
