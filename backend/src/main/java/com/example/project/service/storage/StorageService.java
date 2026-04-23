package com.example.project.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 存储服务接口
 * <p>
 * 提供文件上传、下载和删除的统一接口，支持多种存储后端实现
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface StorageService {

    /**
     * 上传文件到默认目录
     *
     * @param file 要上传的文件
     * @return 上传成功后的文件URL
     */
    String upload(MultipartFile file);

    /**
     * 上传文件到指定目录
     *
     * @param file      要上传的文件
     * @param directory 目标目录名称
     * @return 上传成功后的文件URL
     */
    String upload(MultipartFile file, String directory);

    /**
     * 通过输入流上传文件到默认目录
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @param contentType 文件内容类型
     * @return 上传成功后的文件URL
     */
    String upload(InputStream inputStream, String fileName, String contentType);

    /**
     * 通过输入流上传文件到指定目录
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @param contentType 文件内容类型
     * @param directory   目标目录名称
     * @return 上传成功后的文件URL
     */
    String upload(InputStream inputStream, String fileName, String contentType, String directory);

    /**
     * 下载文件
     *
     * @param fileUrl 文件URL
     * @return 文件字节数组
     */
    byte[] download(String fileUrl);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    void delete(String fileUrl);

    /**
     * 检查文件是否存在
     *
     * @param fileUrl 文件URL
     * @return 存在返回true，否则返回false
     */
    boolean exists(String fileUrl);

    /**
     * 获取文件的完整URL
     *
     * @param relativePath 文件相对路径
     * @return 完整的文件URL
     */
    String getFullUrl(String relativePath);
}
