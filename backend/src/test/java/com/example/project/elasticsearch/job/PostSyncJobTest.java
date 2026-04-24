package com.example.project.elasticsearch.job;

import com.example.project.service.PostSyncService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 帖子同步测试
 * <p>
 * 测试 ES 同步功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Slf4j
@SpringBootTest
public class PostSyncJobTest {

    @Autowired
    private PostSyncService postSyncService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 测试启动时同步检查
     * 模拟应用启动时检查 ES 是否为空，为空则全量同步
     */
    @Test
    public void testInitSync() throws InterruptedException {
        log.info("Testing startup sync check...");

        // 延迟 3 秒，等待 ES 连接就绪
        Thread.sleep(3000);

        long count = postSyncService.getEsPostCount();
        log.info("ES post count: {}", count);

        if (count == 0) {
            log.info("ES is empty, starting full sync...");
            postSyncService.syncAllPosts();
            log.info("Full sync completed");
        } else {
            log.info("ES already has {} posts, skip sync", count);
        }
    }

    /**
     * 测试增量同步
     * 同步最近 10 分钟的数据
     */
    @Test
    public void testIncrementalSync() {
        LocalDateTime lastTime = LocalDateTime.now().minusMinutes(10);
        String lastTimeStr = lastTime.format(FORMATTER);

        log.info("Testing incremental sync - last 10 minutes from: {}", lastTimeStr);

        int synced = postSyncService.syncPostsAfter(lastTimeStr);
        log.info("Incremental sync completed - synced: {} posts", synced);
    }

    /**
     * 测试全量同步
     */
    @Test
    public void testFullSync() {
        log.info("Testing full sync...");
        postSyncService.syncAllPosts();
        log.info("Full sync completed");
    }

    /**
     * 测试获取 ES 数据量
     */
    @Test
    public void testGetEsCount() {
        long count = postSyncService.getEsPostCount();
        log.info("ES post count: {}", count);
    }
}
