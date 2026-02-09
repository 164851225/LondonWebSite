package com.oddfar.campus.common.model.api.db;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ElectricDayTotalDataResponse {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 项目ID
     */
    private String pid;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 园区ID
     */
    private String parkId;

    /**
     * 统计时间
     */
    private String statisticsTime;

    /**
     * 统计数据（保留2位小数）
     */
    private BigDecimal data;

    /**
     * 备注
     */
    private String remark;

}



