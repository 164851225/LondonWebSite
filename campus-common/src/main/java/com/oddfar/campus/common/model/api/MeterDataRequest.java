package com.oddfar.campus.common.model.api;

import lombok.Data;

/**
 * 灯具数据请求实体类
 * 用于封装获取灯具数据接口的请求参数
 */
@Data
public class MeterDataRequest {
    
    /**
     * 园区ID，例如"743192761155654"（六楼）
     */
    private String parkId;
    
    /**
     * 仪表ID，暂未指定时为空字符串
     */
    private String meterId = "";
    
    /**
     * 查询时间，格式为"yyyy-MM-dd"，例如"2025-11-21"
     */
    private String chartTime;
    
    /**
     * 时间查询枚举，0表示默认查询方式
     */
    private String timeQueryEnum ;
}