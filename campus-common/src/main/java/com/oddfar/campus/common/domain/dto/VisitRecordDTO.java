package com.oddfar.campus.common.domain.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 访问记录请求DTO
 */
@Data
public class VisitRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    private Long userId;
    
    /** 网站ID */
    private String webId;
    
    /** 页面URL */
    private String pageUrl;
    
    /** 页面标题 */
    private String pageTitle;
}