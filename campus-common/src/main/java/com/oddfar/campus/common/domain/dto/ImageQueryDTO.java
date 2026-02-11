package com.oddfar.campus.common.domain.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 图片查询请求DTO
 */
@Data
public class ImageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 网站ID */
    private String webId;
    
    /** 位置编码 */
    private String positionCode;

    /** 文件ID */
    private Long fileId;
    
    /** 图片URL */
    private String imageUrl;
    
    /** 图片描述 */
    private String imageDesc;
    
    /** 排序 */
    private Integer sortOrder;
}