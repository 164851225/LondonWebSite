package com.oddfar.campus.common.domain.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 访问统计查询请求DTO
 */
@Data
public class VisitStatsQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    private Long userId;
    
    /** 网站ID */
    private String webId;
    
    /** 开始日期 */
    private Date startDate;
    
    /** 结束日期 */
    private Date endDate;
    
    /** 时间维度类型(hour/day/month) */
    private String periodType;
}