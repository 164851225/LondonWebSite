package com.oddfar.campus.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("db_statistics_data")
public class DbStatisticsDataEntity {
    private static final long serialVersionUID = 1L;

    @TableId(value = "myid")
    private Long myid;

    @TableField(value = "id")
    private Long id;

    @TableField("pid")
    private String pid;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("device_id")
    private String deviceId;

    @TableField("park_id")
    private String parkId;

    @TableField("statistics_time")
    private Date statisticsTime;

    /**
     * 统计数据（保留2位小数）
     */
    @TableField("data")
    private BigDecimal data;

    @TableField("remark")
    private String remark;
}
