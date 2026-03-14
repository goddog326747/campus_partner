package com.example.project.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

    String upload(MultipartFile file);

    String upload(MultipartFile file, String directory);

    String upload(InputStream inputStream, String fileName, String contentType);

    String upload(InputStream inputStream, String fileName, String contentType, String directory);

    byte[] download(String fileUrl);

    void delete(String fileUrl);

    boolean exists(String fileUrl);

    String getFullUrl(String relativePath);
}
