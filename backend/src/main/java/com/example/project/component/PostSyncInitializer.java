package com.example.project.component;

import com.example.project.service.PostSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component
public class PostSyncInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(PostSyncInitializer.class);

    @Autowired
    private PostSyncService postSyncService;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Starting post sync initialization...");
        try {
            // 延迟 10 秒执行，等待 ES 完全启动
            Thread.sleep(10000);
            postSyncService.syncAllPosts();
            logger.info("Post sync initialization completed");
        } catch (Exception e) {
            logger.error("Error during post sync initialization", e);
        }
    }
}
