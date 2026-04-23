package com.example.project.elasticsearch.component;

import com.example.project.service.PostSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 帖子同步初始化器
 * <p>
 * 应用启动时执行全量同步，将 MySQL 数据同步到 Elasticsearch
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostSyncInitializer implements ApplicationRunner {

    private final PostSyncService postSyncService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting post sync initialization...");
        try {
            // 延迟 10 秒执行，等待 ES 完全启动
            Thread.sleep(10000);
            postSyncService.syncAllPosts();
            log.info("Post sync initialization completed");
        } catch (Exception e) {
            log.error("Error during post sync initialization", e);
        }
    }
}
