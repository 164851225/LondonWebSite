package com.oddfar.campus.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户访问记录实体类
 *
 * @author oddfar
 * @since 1.0.0 2024-02-09
 */
@TableName("user_visit_record")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class UserVisitRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 访问记录ID */
    @TableId("visit_id")
    private Long visitId;

    /** 用户ID */
    private Long userId;

    /** 网站ID */
    private String webId;

    /** 页面URL */
    private String pageUrl;

    /** 页面标题 */
    private String pageTitle;

    /** 访问时间 */
    private Date visitTime;

    /** 离开时间 */
    private Date leaveTime;

    /** 停留时长(秒) */
    private Integer duration;

    /** IP地址 */
    private String ipAddress;

    /** 用户代理 */
    private String userAgent;

    /** 设备类型 */
    private String deviceType;

    /** 浏览器 */
    private String browser;

    /** 创建时间 */
    private Date createTime;

    public UserVisitRecordEntity(Long userId, String webId, String pageUrl) {
        this.userId = userId;
        this.webId = webId;
        this.pageUrl = pageUrl;
        this.visitTime = new Date();
        this.createTime = new Date();
    }
}