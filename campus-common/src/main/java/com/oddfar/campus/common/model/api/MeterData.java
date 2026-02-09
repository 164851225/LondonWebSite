package com.oddfar.campus.common.model.api;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 灯具数据项实体类
 * 用于封装灯具数据接口返回的单个数据条目
 */
@Data
public class MeterData {
    
    /**
     * 仪表读数
     */
    private BigDecimal meterReading;
    
    /**
     * 当前能耗
     */
    private BigDecimal currentEnergy;
    
    /**
     * 原始能耗
     */
    private BigDecimal originalEnergy;
    
    /**
     * 节约能耗
     */
    private BigDecimal savingEnergy;
    
    /**
     * 数据时间，格式为"yyyy-MM-DD"
     */
    private String chartTime;
}