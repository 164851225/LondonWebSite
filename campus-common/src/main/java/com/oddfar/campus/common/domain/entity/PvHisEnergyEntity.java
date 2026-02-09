package com.oddfar.campus.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("pv_his_energy")
public class PvHisEnergyEntity {

    /**
     * 自增主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 电站唯一键
     */
    @TableField("ps_key")
    private String psKey;

    /**
     * 指标类型
     * p83022：电站日发电量(Wh)
     * p83033：电站功率(W)
     * p83024：电站累计发电(Wh)
     */
    @TableField("type")
    private String type;

    /**
     * 设备数据时间戳（Unix 秒/毫秒）
     */
    @TableField("time_stamp")
    private Long timeStamp;

    /**
     * 发电量/功率值（kWh 或 W，根据 type 区分）
     */
    @TableField("value")
    private BigDecimal value;
    /**
     * 记录写入时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 记录更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;
}