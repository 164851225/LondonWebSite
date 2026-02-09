package com.oddfar.campus.common.domain.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class VisitStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 基础统计字段
    private Long userId;
    private String webId;
    private Date statsDate;
    private Integer visitCount;
    private Integer totalDuration;
    private Double avgDuration;
    private Integer pageCount;
    
    // 今日统计
    private Integer todayVisitCount;
    private Integer todayTotalDuration;
    private Double todayAvgDuration;
    
    // 月度统计
    private Integer monthlyVisitCount;
    private Integer lastMonthVisitCount;
    private BigDecimal monthGrowthRate; // 环比增长率
    
    // 趋势数据
    private List<TrendDataVO> trendData;
    
    // 时间范围
    private Date startDate;
    private Date endDate;
    
    @Data
    public static class TrendDataVO {
        private String timeLabel; // 时间标签
        private Integer visitCount; // 访问量
        private Integer uniqueUsers; // 独立用户数
        private Integer totalDuration; // 总时长
        private Double avgDuration; // 平均时长
    }
}