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
@TableName("pv_today_energy")
public class PvTodayEnergyEntity {

    /**
     * 自增主键
     */
    @TableId("id")
    private Long id;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备故障状态码
     */
    @TableField("dev_fault_status")
    private Integer devFaultStatus;

    /**
     * 电站唯一键
     */
    @TableField("ps_key")
    private String psKey;

    /**
     * 设备序列号
     */
    @TableField("device_sn")
    private String deviceSn;

    /**
     * 设备运行状态 1-正常
     */
    @TableField("dev_status")
    private Integer devStatus;

    /**
     * 电站ID（分区键）
     */
    @TableField("ps_id")
    private String psId;

    /**
     * 通信设备序列号
     */
    @TableField("communication_dev_sn")
    private String communicationDevSn;

    /**
     * 累计发电量(kWh)
     */
    @TableField("p83024")
    private BigDecimal p83024;

    /**
     * 设备维度唯一ID
     */
    @TableField("uuid")
    private String uuid;

    /**
     * 当日发电量(kWh)
     */
    @TableField("p83022")
    private BigDecimal p83022;

    /**
     * 输入功率(W)
     */
    @TableField("p83033")
    private BigDecimal p83033;

    /**
     * 设备数据时间戳
     */
    @TableField("device_time")
    private Long deviceTime;

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