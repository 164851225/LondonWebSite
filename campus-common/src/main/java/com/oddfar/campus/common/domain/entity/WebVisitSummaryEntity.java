package com.oddfar.campus.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 网站访问汇总统计实体类
 *
 * @author oddfar
 * @since 1.0.0 2024-02-09
 */
@TableName("web_visit_summary")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class WebVisitSummaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 汇总ID */
    @TableId("summary_id")
    private Long summaryId;

    /** 网站ID */
    private String webId;

    /** 统计日期 */
    private Date statsDate;

    /** 总访问量 */
    private Integer totalVisits;

    /** 独立访客数 */
    private Integer uniqueUsers;

    /** 总停留时长(秒) */
    private Integer totalDuration;

    /** 平均停留时长(秒) */
    private Integer avgDuration;

    /** 跳出率(%) */
    private BigDecimal bounceRate;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public WebVisitSummaryEntity(String webId, Date statsDate) {
        this.webId = webId;
        this.statsDate = statsDate;
        this.totalVisits = 0;
        this.uniqueUsers = 0;
        this.totalDuration = 0;
        this.avgDuration = 0;
        this.bounceRate = BigDecimal.ZERO;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}