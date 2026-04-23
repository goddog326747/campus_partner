package com.example.project.service.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private String type = "local";

    private Local local = new Local();

    private Oss oss = new Oss();

    private List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    );

    private long maxFileSize = 10 * 1024 * 1024;

    @Data
    public static class Local {
        private String basePath = "C:/Users/void/Downloads/project/img";
        private String urlPrefix = "/img";
    }

    @Data
    public static class Oss {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String urlPrefix;
    }
}
