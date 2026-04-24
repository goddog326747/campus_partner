package com.example.project.elasticsearch.controller;

import com.example.project.common.Result;
import com.example.project.service.PostSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 帖子同步管理接口
 * <p>
 * 提供手动触发同步的接口，用于管理维护
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class PostSyncController {

    private final PostSyncService postSyncService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取 ES 中的帖子数量
     */
    @GetMapping("/count")
    public Result<Long> getEsCount() {
        long count = postSyncService.getEsPostCount();
        return Result.success(count);
    }

    /**
     * 手动触发全量同步
     */
    @PostMapping("/full")
    public Result<String> fullSync() {
        log.info("Manual full sync triggered");
        try {
            postSyncService.syncAllPosts();
            return Result.success("全量同步已启动");
        } catch (Exception e) {
            log.error("Manual full sync failed", e);
            return Result.error(500, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发增量同步
     * 
     * @param minutes 同步最近多少分钟的数据，默认 5 分钟
     */
    @PostMapping("/incremental")
    public Result<String> incrementalSync(@RequestParam(defaultValue = "5") int minutes) {
        LocalDateTime lastTime = LocalDateTime.now().minusMinutes(minutes);
        String lastTimeStr = lastTime.format(FORMATTER);
        
        log.info("Manual incremental sync triggered - last {} minutes", minutes);
        try {
            int synced = postSyncService.syncPostsAfter(lastTimeStr);
            return Result.success("增量同步完成，同步了 " + synced + " 条数据");
        } catch (Exception e) {
            log.error("Manual incremental sync failed", e);
            return Result.error(500, "同步失败: " + e.getMessage());
        }
    }
}
