package com.oddfar.campus.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户每日统计实体类
 *
 * @author oddfar
 * @since 1.0.0 2024-02-09
 */
@TableName("user_daily_stats")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class UserDailyStatsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 统计ID */
    @TableId("stats_id")
    private Long statsId;

    /** 用户ID */
    private Long userId;

    /** 网站ID */
    private String webId;

    /** 统计日期 */
    private Date statsDate;

    /** 访问次数 */
    private Integer visitCount;

    /** 总停留时长(秒) */
    private Integer totalDuration;

    /** 平均停留时长(秒) */
    private Integer avgDuration;

    /** 访问页面数 */
    private Integer pageCount;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public UserDailyStatsEntity( String webId, Date statsDate) {
        this.webId = webId;
        this.statsDate = statsDate;
        this.visitCount = 0;
        this.totalDuration = 0;
        this.avgDuration = 0;
        this.pageCount = 0;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}