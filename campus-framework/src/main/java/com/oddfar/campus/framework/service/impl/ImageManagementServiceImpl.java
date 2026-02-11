package com.oddfar.campus.framework.service.impl;

import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.domain.entity.FileInfoEntity;
import com.oddfar.campus.common.domain.entity.WebImagePositionEntity;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.SecurityUtils;
import com.oddfar.campus.common.utils.ServletUtils;
import com.oddfar.campus.common.utils.StringUtils;
import com.oddfar.campus.framework.mapper.FileInfoMapper;
import com.oddfar.campus.framework.mapper.WebImagePositionMapper;
import com.oddfar.campus.framework.service.ImageManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImageManagementServiceImpl implements ImageManagementService {

    @Value("${file.upload.path:/opt/app/img}")
    private String uploadPath;

    @Value("${file.access.url-prefix:/api/file/download/}")
    private String urlPrefix;

    @Resource
    private FileInfoMapper fileInfoMapper;

    @Resource
    private WebImagePositionMapper webImagePositionMapper;

    @Override
    @Transactional
    public FileInfoEntity uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String newFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path storagePath = Paths.get(uploadPath, datePath);
            
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            Path filePath = storagePath.resolve(newFileName);
            file.transferTo(filePath);

            FileInfoEntity fileInfo = new FileInfoEntity();
            fileInfo.setFileName(originalFilename);
            fileInfo.setFilePath(datePath + "/" + newFileName);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(file.getContentType());
            fileInfo.setFileExtension(fileExtension);
            fileInfo.setStoragePath(filePath.toString());
            fileInfo.setAccessUrl(urlPrefix + datePath + "/" + newFileName);

            fileInfoMapper.insert(fileInfo);
            return fileInfo;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new ServiceException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadFile(Long fileId) {
        FileInfoEntity fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new ServiceException("文件不存在");
        }

        try {
            Path filePath = Paths.get(fileInfo.getStoragePath());
            if (!Files.exists(filePath)) {
                throw new ServiceException("文件不存在于服务器");
            }

            HttpServletResponse response = ServletUtils.getResponse();
            response.setContentType(fileInfo.getFileType());
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                URLEncoder.encode(fileInfo.getFileName(), "UTF-8"));
            response.setContentLengthLong(fileInfo.getFileSize());

            Files.copy(filePath, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new ServiceException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public WebImagePositionEntity getImageByPositionCode(String webId, String positionCode) {
        return webImagePositionMapper.selectOne(
            new LambdaQueryWrapperX<WebImagePositionEntity>()
                .eq(WebImagePositionEntity::getWebId, webId)
                .eq(WebImagePositionEntity::getPositionCode, positionCode)
                .eq(WebImagePositionEntity::getStatus, 1)
                .eq(WebImagePositionEntity::getDelFlag, 0)
        );
    }

    @Override
    public List<WebImagePositionEntity> getImageListByWebId(String webId) {
        return webImagePositionMapper.selectList(
            new LambdaQueryWrapperX<WebImagePositionEntity>()
                .eq(WebImagePositionEntity::getWebId, webId)
                .eq(WebImagePositionEntity::getStatus, 1)
                .eq(WebImagePositionEntity::getDelFlag, 0)
                .orderByAsc(WebImagePositionEntity::getSortOrder)
        );
    }

    @Override
    @Transactional
    public boolean saveOrUpdateImagePosition(WebImagePositionEntity entity) {
        try {
            // 参数校验
            if (entity == null || StringUtils.isEmpty(entity.getWebId()) || 
                StringUtils.isEmpty(entity.getPositionCode())) {
                throw new ServiceException("参数不完整，缺少必要字段");
            }
            
            // 查询是否存在相同位置的记录
            WebImagePositionEntity existingPosition = getImageByPositionCode(entity.getWebId(), entity.getPositionCode());
            
            if (existingPosition != null) {
                // 存在则更新
                entity.setPositionId(existingPosition.getPositionId());
                entity.setUpdateTime(new Date());
                webImagePositionMapper.updateById(entity);
                log.info("更新图片位置成功: webId={}, positionCode={}", entity.getWebId(), entity.getPositionCode());
            } else {
                // 不存在则新增
                entity.setCreateTime(new Date());
                entity.setStatus(1); // 默认启用
                entity.setDelFlag(0); // 默认未删除
                if (entity.getSortOrder() == null) {
                    entity.setSortOrder(0); // 默认排序
                }
                webImagePositionMapper.insert(entity);
                log.info("新增图片位置成功: webId={}, positionCode={}", entity.getWebId(), entity.getPositionCode());
            }
            return true;
        } catch (Exception e) {
            log.error("保存或更新图片位置异常", e);
            throw new ServiceException("保存或更新图片位置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<FileInfoEntity> uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ServiceException("上传文件列表不能为空");
        }
        
        List<FileInfoEntity> uploadedFiles = new ArrayList<>();
        
        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    log.warn("跳过空文件");
                    continue;
                }
                
                try {
                    FileInfoEntity fileInfo = uploadSingleFile(file);
                    uploadedFiles.add(fileInfo);
                    log.info("文件上传成功: {}", file.getOriginalFilename());
                } catch (Exception e) {
                    log.error("单个文件上传失败: {}", file.getOriginalFilename(), e);
                    // 继续处理其他文件，不中断整个批量操作
                }
            }
            
            if (uploadedFiles.isEmpty()) {
                throw new ServiceException("没有文件上传成功");
            }
            
            return uploadedFiles;
            
        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            throw new ServiceException("批量文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 单个文件上传（内部方法）
     * @param file 文件
     * @return 文件信息
     */
    private FileInfoEntity uploadSingleFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String newFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path storagePath = Paths.get(uploadPath, datePath);
            
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            Path filePath = storagePath.resolve(newFileName);
            file.transferTo(filePath);

            FileInfoEntity fileInfo = new FileInfoEntity();
            fileInfo.setFileName(originalFilename);
            fileInfo.setFilePath(datePath + "/" + newFileName);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(file.getContentType());
            fileInfo.setFileExtension(fileExtension);
            fileInfo.setStoragePath(filePath.toString());
            fileInfo.setAccessUrl(urlPrefix + datePath + "/" + newFileName);

            fileInfoMapper.insert(fileInfo);
            return fileInfo;

        } catch (Exception e) {
            throw new ServiceException("文件上传失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }
}