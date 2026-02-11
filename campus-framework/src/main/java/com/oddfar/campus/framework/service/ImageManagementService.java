package com.oddfar.campus.framework.service;

import com.oddfar.campus.common.domain.entity.FileInfoEntity;
import com.oddfar.campus.common.domain.entity.WebImagePositionEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ImageManagementService {
    FileInfoEntity uploadFile(MultipartFile file);
    void downloadFile(Long fileId);
    WebImagePositionEntity getImageByPositionCode(String webId, String positionCode);
    List<WebImagePositionEntity> getImageListByWebId(String webId);
    
    /**
     * 添加或更新图片位置信息
     * @param entity 图片位置实体
     * @return 是否成功
     */
    boolean saveOrUpdateImagePosition(WebImagePositionEntity entity);
}