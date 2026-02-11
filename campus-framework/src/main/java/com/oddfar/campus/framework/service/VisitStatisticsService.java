package com.oddfar.campus.framework.service;

import com.oddfar.campus.common.domain.entity.UserVisitRecordEntity;
import com.oddfar.campus.common.domain.entity.UserDailyStatsEntity;
import com.oddfar.campus.common.domain.entity.WebVisitSummaryEntity;
import com.oddfar.campus.common.domain.vo.VisitStatsVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface VisitStatisticsService {
    
    /**
     * 记录用户访问
     */
    UserVisitRecordEntity recordVisit(  String webId, String pageUrl, String pageTitle);
    
    /**
     * 更新用户停留时长
     */
    void updateDuration(Long visitId, Integer duration);
    
    /**
     * 获取用户今日访问统计
     */
    VisitStatsVO getUserTodayStats( String webId);
    
    /**
     * 获取用户天平均停留时间
     */
    BigDecimal getUserAvgDuration( String webId, Date startDate, Date endDate);
    
    /**
     * 获取本月累计访问及环比
     */
    VisitStatsVO getMonthlyVisitStats( String webId);
    
    /**
     * 获取访问趋势数据
     */
    List<VisitStatsVO.TrendDataVO> getVisitTrend(String webId, String periodType, Date startDate, Date endDate);
    
    /**
     * 统计并更新每日数据
     */
    void calculateDailyStats(Date statsDate);
    
    /**
     * 统计并更新网站汇总数据
     */
    void calculateWebSummary(Date statsDate);
}