package com.oddfar.campus.common.model.api.db;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ElectricChartDataResponse {



    /**
     * 统计时间
     */
    private Date chartTime;

    /**
     * 统计数据（保留2位小数）
     */
    private BigDecimal data;


}



