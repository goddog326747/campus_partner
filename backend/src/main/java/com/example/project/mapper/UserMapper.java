package com.example.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 * <p>
 * 提供用户表的数据访问操作，继承MyBatis-Plus的BaseMapper接口
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    List<User> selectAll();
}
