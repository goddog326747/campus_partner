package com.example.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.entity.Post;

/**
 * 帖子数据访问接口
 * <p>
 * 提供帖子表的数据访问操作，继承MyBatis-Plus的BaseMapper接口
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface PostMapper extends BaseMapper<Post> {
}
