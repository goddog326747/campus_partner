package com.example.project.service.storage;

import com.example.project.service.storage.StorageProperties.Local;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service("localStorageService")
public class LocalStorageServiceImpl implements StorageService {

    private final Local localConfig;
    private final StorageProperties storageProperties;

    public LocalStorageServiceImpl(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.localConfig = storageProperties.getLocal();
        init();
    }

    private void init() {
        Path basePath = Paths.get(localConfig.getBasePath());
        try {
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("Created storage directory: {}", basePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory: " + basePath, e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        return upload(file, null);
    }

    @Override
    public String upload(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        validateFile(extension, file.getSize());

        String fileName = generateFileName(extension);
        String relativePath = buildRelativePath(fileName, directory);

        try {
            Path targetPath = Paths.get(localConfig.getBasePath(), relativePath);
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded successfully: {}", targetPath);
        } catch (IOException e) {
            log.error("Failed to upload file: {}", originalFilename, e);
            throw new RuntimeException("Failed to upload file: " + originalFilename, e);
        }

        return getFullUrl(relativePath);
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        return upload(inputStream, fileName, contentType, null);
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType, String directory) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        String extension = getFileExtension(fileName);
        validateExtension(extension);

        String newFileName = generateFileName(extension);
        String relativePath = buildRelativePath(newFileName, directory);

        try {
            Path targetPath = Paths.get(localConfig.getBasePath(), relativePath);
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded successfully: {}", targetPath);
        } catch (IOException e) {
            log.error("Failed to upload file: {}", fileName, e);
            throw new RuntimeException("Failed to upload file: " + fileName, e);
        }

        return getFullUrl(relativePath);
    }

    @Override
    public byte[] download(String fileUrl) {
        String relativePath = extractRelativePath(fileUrl);
        Path filePath = Paths.get(localConfig.getBasePath(), relativePath);

        try {
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found: " + fileUrl);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to download file: {}", fileUrl, e);
            throw new RuntimeException("Failed to download file: " + fileUrl, e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        String relativePath = extractRelativePath(fileUrl);
        Path filePath = Paths.get(localConfig.getBasePath(), relativePath);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            throw new RuntimeException("Failed to delete file: " + fileUrl, e);
        }
    }

    @Override
    public boolean exists(String fileUrl) {
        String relativePath = extractRelativePath(fileUrl);
        Path filePath = Paths.get(localConfig.getBasePath(), relativePath);
        return Files.exists(filePath);
    }

    @Override
    public String getFullUrl(String relativePath) {
        return localConfig.getUrlPrefix() + "/" + relativePath.replace("\\", "/");
    }

    private String extractRelativePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("File URL cannot be empty");
        }

        String urlPrefix = localConfig.getUrlPrefix();
        if (fileUrl.startsWith(urlPrefix)) {
            return fileUrl.substring(urlPrefix.length() + 1);
        }
        return fileUrl;
    }

    private String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private String buildRelativePath(String fileName, String directory) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        if (directory != null && !directory.isEmpty()) {
            return directory + "/" + datePath + "/" + fileName;
        }
        return datePath + "/" + fileName;
    }

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

    private void validateFile(String extension, long fileSize) {
        validateExtension(extension);
        validateFileSize(fileSize);
    }

    private void validateExtension(String extension) {
        if (!storageProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("File extension not allowed: " + extension);
        }
    }

    private void validateFileSize(long fileSize) {
        if (fileSize > storageProperties.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size: " + fileSize);
        }
    }
}
