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
}