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
@TableName("light_energy")
public class LightEnergyEntity {
    private static final long serialVersionUID = 1L;

    //    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId("id")
    private Long id;

    @TableField("meter_reading")
    private BigDecimal meterReading;

    @TableField("current_energy")
    private BigDecimal currentEnergy;

    @TableField("original_energy")
    private BigDecimal originalEnergy;

    @TableField("saving_energy")
    private BigDecimal savingEnergy;

    @TableField("chart_time")
    private Date chartTime;

    @TableField("create_time")
    private Date createTime;


    @TableField("park_id")
    private String parkId;


    @TableField("meter_id")
    private String meterId;


    @TableField("time_query_enum")
    private String timeQueryEnum;


}
