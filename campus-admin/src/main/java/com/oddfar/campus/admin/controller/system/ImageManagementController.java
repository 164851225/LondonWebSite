package com.oddfar.campus.admin.controller.system;

import com.oddfar.campus.common.annotation.Anonymous;
import com.oddfar.campus.common.annotation.Log;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.domain.dto.ImageQueryDTO;
import com.oddfar.campus.common.domain.entity.FileInfoEntity;
import com.oddfar.campus.common.domain.entity.WebImagePositionEntity;
import com.oddfar.campus.common.enums.BusinessStatus;
import com.oddfar.campus.framework.service.ImageManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/system/image")
@Slf4j
public class ImageManagementController {

    @Resource
    private ImageManagementService imageManagementService;

    /**
     * 上传文件
     * @param file 上传的文件
     * @return 文件信息
     */
    @PostMapping("/upload")
    @Anonymous
    public R uploadFile(@RequestPart("file") MultipartFile file) {
        try {
            FileInfoEntity fileInfo = imageManagementService.uploadFile(file);
            return R.ok("上传成功", fileInfo);
        } catch (Exception e) {
            log.error("文件上传异常", e);
            return R.error(e.getMessage());
        }
    }

    /**
     * 下载文件
     * @param id 文件ID
     */
    @GetMapping("/download/{id}")
    @Anonymous
    public void downloadFile(@PathVariable("id") Long id) {
        imageManagementService.downloadFile(id);
    }

    /**
     * 根据网站ID和位置编码获取图片信息
     * @param dto 图片查询请求参数
     * @return 图片位置信息
     */
    @PostMapping("/position")
    @Anonymous
    public R getImageByPosition(@RequestBody ImageQueryDTO dto) {
        try {
            WebImagePositionEntity position = imageManagementService.getImageByPositionCode(dto.getWebId(), dto.getPositionCode());
            return R.ok(position);
        } catch (Exception e) {
            log.error("获取图片位置信息异常", e);
            return R.error(e.getMessage());
        }
    }

    /**
     * 添加或更新图片位置信息
     * @param dto 图片位置请求参数
     * @return 操作结果
     */
    @PostMapping("/add/position")
    @Anonymous
    @Log
    public R addImageByPosition(@RequestBody ImageQueryDTO dto) {
        try {
            // 参数校验
            if (dto == null || dto.getWebId() == null || dto.getPositionCode() == null) {
                return R.error("参数不完整，缺少必要字段");
            }
            
            // 构建实体对象
            WebImagePositionEntity entity = new WebImagePositionEntity();
            entity.setWebId(dto.getWebId());
            entity.setPositionCode(dto.getPositionCode());
            entity.setImageUrl(dto.getImageUrl());
            entity.setImageDesc(dto.getImageDesc());
            entity.setSortOrder(dto.getSortOrder());
            entity.setFileId(dto.getFileId());
            
            // 调用服务层方法进行保存或更新
            boolean result = imageManagementService.saveOrUpdateImagePosition(entity);
            
            return R.ok("图片位置保存成功");
        } catch (Exception e) {
            log.error("添加或更新图片位置异常", e);
            return R.error(e.getMessage());
        }
    }


    /**
     * 获取指定网站的图片列表
     * @param dto 图片查询请求参数
     * @return 图片位置列表
     */
    @PostMapping("/list")
    @Anonymous
    public R getImageList(@RequestBody ImageQueryDTO dto) {
        try {
            List<WebImagePositionEntity> list = imageManagementService.getImageListByWebId(dto.getWebId());
            return R.ok(list);
        } catch (Exception e) {
            log.error("获取网站图片列表异常", e);
            return R.error(e.getMessage());
        }
    }
}