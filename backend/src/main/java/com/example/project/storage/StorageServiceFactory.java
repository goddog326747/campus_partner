package com.example.project.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Component
public class StorageServiceFactory {

    private final Map<String, StorageService> storageServices;
    private final StorageProperties storageProperties;

    private StorageService currentStorageService;

    public StorageServiceFactory(Map<String, StorageService> storageServices, StorageProperties storageProperties) {
        this.storageServices = storageServices;
        this.storageProperties = storageProperties;
    }

    @PostConstruct
    public void init() {
        String type = storageProperties.getType();
        log.info("Initializing storage service with type: {}", type);

        String beanName = type.equalsIgnoreCase("oss") ? "ossStorageService" : "localStorageService";
        this.currentStorageService = storageServices.get(beanName);

        if (this.currentStorageService == null) {
            log.warn("Storage service '{}' not found, falling back to local storage", beanName);
            this.currentStorageService = storageServices.get("localStorageService");
        }

        log.info("Storage service initialized: {}", this.currentStorageService.getClass().getSimpleName());
    }

    public StorageService getStorageService() {
        return currentStorageService;
    }

    public StorageService getStorageService(String type) {
        String beanName = type.equalsIgnoreCase("oss") ? "ossStorageService" : "localStorageService";
        StorageService service = storageServices.get(beanName);
        if (service == null) {
            throw new IllegalArgumentException("Storage service not found: " + type);
        }
        return service;
    }

    public String getCurrentStorageType() {
        return storageProperties.getType();
    }
}
