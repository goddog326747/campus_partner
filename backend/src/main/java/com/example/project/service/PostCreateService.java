package com.example.project.service;

import com.example.project.dto.PostCreateRequest;
import com.example.project.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 帖子创建服务接口
 * <p>
 * 提供帖子创建和删除相关的服务，包括图片上传功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface PostCreateService {
    
    /**
     * 根据请求对象创建帖子
     *
     * @param request 帖子创建请求对象
     * @return 创建成功后的帖子对象
     */
    Post createPost(PostCreateRequest request);
    
    /**
     * 创建新帖子
     *
     * @param post 帖子对象
     * @return 创建成功返回true，否则返回false
     */
    boolean createPost(Post post);
    
    /**
     * 上传帖子图片
     *
     * @param files 图片文件列表
     * @return 上传成功后的图片URL列表
     */
    List<String> uploadPostImages(List<MultipartFile> files);
    
    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     */
    void deletePost(Long postId);
}
