package com.example.project.service;

import com.example.project.common.PageResult;
import com.example.project.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 帖子服务接口
 * <p>
 * 提供帖子的增删改查服务
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface PostService {
    
    /**
     * 获取帖子列表（简单查询，分页）
     *
     * @param category 分类筛选条件，可为null
     * @param keyword  关键词搜索条件，可为null
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页帖子列表
     */
    PageResult<Post> listPostsWithPage(String category, String keyword, int pageNum, int pageSize);
    
    /**
     * 获取帖子列表（高级筛选，分页）
     *
     * @param category  分类筛选条件，可为null
     * @param keyword   关键词搜索条件，可为null
     * @param location  地点筛选条件，可为null
     * @param school    学校筛选条件，可为null
     * @param verified  是否已认证筛选，可为null
     * @param gender    性别筛选，可为null
     * @param pageNum   页码（从1开始）
     * @param pageSize  每页大小
     * @return 分页帖子列表
     */
    PageResult<Post> listPostsWithFilter(String category, String keyword, String location, String school, Boolean verified, Integer gender, int pageNum, int pageSize);
    
    /**
     * 创建新帖子
     *
     * @param post 帖子对象
     * @return 创建成功返回true，否则返回false
     */
    boolean createPost(Post post);
    
    /**
     * 根据ID获取帖子详情
     *
     * @param id 帖子ID
     * @return 帖子对象，不存在则返回null
     */
    Post getPostById(Long id);
    
    /**
     * 获取指定用户发布的帖子列表
     *
     * @param userId 用户ID
     * @return 该用户发布的帖子列表
     */
    List<Post> getPostsByUserId(Long userId);
    
    /**
     * 上传帖子图片
     *
     * @param files 图片文件列表
     * @return 上传成功后的图片URL列表
     */
    List<String> uploadImages(List<MultipartFile> files);
    
    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     */
    void deletePost(Long postId);
}
