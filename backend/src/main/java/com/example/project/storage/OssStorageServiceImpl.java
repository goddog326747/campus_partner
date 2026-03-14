package com.example.project.storage;

import com.example.project.storage.StorageProperties.Oss;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service("ossStorageService")
public class OssStorageServiceImpl implements StorageService {

    private final Oss ossConfig;

    public OssStorageServiceImpl(StorageProperties storageProperties) {
        this.ossConfig = storageProperties.getOss();
    }

    @Override
    public String upload(MultipartFile file) {
        throw new UnsupportedOperationException("OSS storage not implemented yet. Please use local storage.");
    }

    @Override
    public String upload(MultipartFile file, String directory) {
        throw new UnsupportedOperationException("OSS storage not implemented yet. Please use local storage.");
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        throw new UnsupportedOperationException("OSS storage not implemented yet. Please use local storage.");
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType, String directory) {
        throw new UnsupportedOperationException("OSS storage not implemented yet. Please use local storage.");
    }

    @Override
    public byte[] download(String fileUrl) {
        throw new UnsupportedOperationException("OSS storage not implemented yet. Please use local storage.");
    }

    @Override
    public void delete(String fileUrl) {
        throw new UnsupportedOperationException("OSS storage not implemented yet. Please use local storage.");
    }

    @Override
    public boolean exists(String fileUrl) {
        throw new UnsupportedOperationException("OSS storage not implemented yet. Please use local storage.");
    }

    @Override
    public String getFullUrl(String relativePath) {
        if (ossConfig.getUrlPrefix() != null && !ossConfig.getUrlPrefix().isEmpty()) {
            return ossConfig.getUrlPrefix() + "/" + relativePath;
        }
        return relativePath;
    }
}
