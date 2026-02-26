package com.oddfar.campus.framework.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.oddfar.campus.common.domain.entity.UserVisitRecordEntity;
import com.oddfar.campus.common.domain.entity.UserDailyStatsEntity;
import com.oddfar.campus.common.domain.entity.WebVisitSummaryEntity;
import com.oddfar.campus.common.domain.vo.VisitStatsVO;
import com.oddfar.campus.common.utils.DateUtils;
import com.oddfar.campus.common.utils.ip.IpUtils;
import com.oddfar.campus.common.utils.ServletUtils;
import com.oddfar.campus.framework.mapper.UserVisitRecordMapper;
import com.oddfar.campus.framework.mapper.UserDailyStatsMapper;
import com.oddfar.campus.framework.mapper.WebVisitSummaryMapper;
import com.oddfar.campus.framework.service.VisitStatisticsService;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VisitStatisticsServiceImpl implements VisitStatisticsService {

    @Resource
    private UserVisitRecordMapper visitRecordMapper;
    
    @Resource
    private UserDailyStatsMapper dailyStatsMapper;
    
    @Resource
    private WebVisitSummaryMapper webSummaryMapper;

    @Override
    @Transactional
    public UserVisitRecordEntity recordVisit( String webId, String pageUrl, String pageTitle) {
        try {
            UserVisitRecordEntity record = new UserVisitRecordEntity( webId, pageUrl);
            record.setPageTitle(pageTitle);
            
            // 获取客户端信息
            String userAgentStr = ServletUtils.getRequest().getHeader("User-Agent");
            if (userAgentStr != null) {
                UserAgent userAgent = UserAgent.parseUserAgentString(userAgentStr);
                record.setUserAgent(userAgentStr);
                record.setDeviceType(userAgent.getOperatingSystem().getDeviceType().getName());
                record.setBrowser(userAgent.getBrowser().getName());
            }
            
            record.setIpAddress(IpUtils.getIpAddr());
            visitRecordMapper.insert(record);
            
            // 更新当日统计数据
            updateDailyStats( webId, new Date());
            
            return record;
        } catch (Exception e) {
            log.error("记录访问失败", e);
            return null;
        }
    }

    @Override
    public void updateDuration(Long visitId, Integer duration) {
        try {
            UserVisitRecordEntity record = visitRecordMapper.selectById(visitId);
            
            if (record == null) {
                // 如果记录不存在，创建新记录
                record = new UserVisitRecordEntity();
                record.setVisitId(visitId);
                record.setDuration(duration);
                record.setVisitTime(new Date());
                record.setLeaveTime(new Date());
                record.setWebId("1");
                visitRecordMapper.insert(record);
            } else {
                record.setDuration(duration+record.getDuration());
                record.setVisitTime(new Date());
                record.setLeaveTime(new Date());
                visitRecordMapper.updateById(record);
            }
            

        } catch (Exception e) {
            log.error("更新停留时长失败", e);
        }
    }

    @Autowired
    private UserVisitRecordMapper userVisitRecordMapper;
    @Override
    public VisitStatsVO getUserTodayStats( String webId) {
        Date today = getToday();
        UserDailyStatsEntity todayStats = dailyStatsMapper.selectOne(
            new LambdaQueryWrapper<UserDailyStatsEntity>()
                .eq(UserDailyStatsEntity::getWebId, webId)
                .eq(UserDailyStatsEntity::getStatsDate, today)
        );
        
        VisitStatsVO vo = new VisitStatsVO();
        vo.setWebId(webId);
        vo.setStatsDate(today);
        Double l = userVisitRecordMapper.totalAvgDuration();
        if (todayStats != null) {
            vo.setTodayVisitCount(todayStats.getVisitCount());
            vo.setTodayTotalDuration(todayStats.getTotalDuration());
            vo.setTodayAvgDuration(l != null ? l : 0.0);
        }
        
        return vo;
    }

    @Override
    public BigDecimal getUserAvgDuration( String webId, Date startDate, Date endDate) {
//        List<UserDailyStatsEntity> statsList = dailyStatsMapper.selectList(
//            new LambdaQueryWrapper<UserDailyStatsEntity>()
//                .eq(UserDailyStatsEntity::getWebId, webId)
//                .ge(UserDailyStatsEntity::getStatsDate, startDate)
//                .le(UserDailyStatsEntity::getStatsDate, endDate)
//        );
        
//        if (statsList.isEmpty()) {
//            return BigDecimal.ZERO;
//        }
        Double l = userVisitRecordMapper.totalAvgDuration();

//        BigDecimal totalDuration = statsList.stream()
//            .map(stat -> stat.getTotalDuration() != null ? new BigDecimal(stat.getTotalDuration()) : BigDecimal.ZERO)
//            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return new BigDecimal(l).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public VisitStatsVO getMonthlyVisitStats( String webId) {
        Date currentMonthStart = getMonthStart();
        Date currentMonthEnd = getMonthEnd();
        Date lastMonthStart = getLastMonthStart();
        Date lastMonthEnd = getLastMonthEnd();
        
        // 本月数据
        List<UserDailyStatsEntity> currentMonthStats = dailyStatsMapper.selectList(
            new LambdaQueryWrapper<UserDailyStatsEntity>()
                .eq(UserDailyStatsEntity::getWebId, webId)
                .ge(UserDailyStatsEntity::getStatsDate, currentMonthStart)
                .le(UserDailyStatsEntity::getStatsDate, currentMonthEnd)
        );
        
        // 上月数据
        List<UserDailyStatsEntity> lastMonthStats = dailyStatsMapper.selectList(
            new LambdaQueryWrapper<UserDailyStatsEntity>()
                .eq(UserDailyStatsEntity::getWebId, webId)
                .ge(UserDailyStatsEntity::getStatsDate, lastMonthStart)
                .le(UserDailyStatsEntity::getStatsDate, lastMonthEnd)
        );
        
        int currentMonthTotal = currentMonthStats.stream()
            .mapToInt(stat -> stat.getVisitCount() != null ? stat.getVisitCount() : 0)
            .sum();
            
        int lastMonthTotal = lastMonthStats.stream()
            .mapToInt(stat -> stat.getVisitCount() != null ? stat.getVisitCount() : 0)
            .sum();
        
        BigDecimal growthRate = BigDecimal.ZERO;
        if (lastMonthTotal > 0) {
            growthRate = new BigDecimal(currentMonthTotal - lastMonthTotal)
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(lastMonthTotal), 2, RoundingMode.HALF_UP);
        }
        
        VisitStatsVO vo = new VisitStatsVO();
        vo.setWebId(webId);
        vo.setMonthlyVisitCount(currentMonthTotal);
        vo.setLastMonthVisitCount(lastMonthTotal);
        vo.setMonthGrowthRate(growthRate);
        
        return vo;
    }

    @Override
    public List<VisitStatsVO.TrendDataVO> getVisitTrend( String webId, String periodType, Date startDate, Date endDate) {
        List<VisitStatsVO.TrendDataVO> trendData = new ArrayList<>();
        
        try {
            switch (periodType.toLowerCase()) {
                case "hour": // 按小时统计
                    trendData = getHourTrendData( webId, startDate, endDate);
                    break;
                case "day": // 按天统计
                    trendData = getDayTrendData( webId, startDate, endDate);
                    break;
                case "month": // 按月统计
                    trendData = getMonthTrendData( webId, startDate, endDate);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的时间维度类型: " + periodType);
            }
        } catch (Exception e) {
            log.error("获取访问趋势数据异常", e);
        }
        
        return trendData;
    }

    @Override
    public void calculateDailyStats(Date statsDate) {
        // 实现每日统计计算逻辑
    }

    @Override
    public void calculateWebSummary(Date statsDate) {
        // 实现网站汇总统计计算逻辑
    }

    private void updateDailyStats( String webId, Date visitDate) {
        Date statsDate = getDateStart(visitDate);
        UserDailyStatsEntity stats = dailyStatsMapper.selectOne(
            new LambdaQueryWrapper<UserDailyStatsEntity>()
                .eq(UserDailyStatsEntity::getWebId, webId)
                .eq(UserDailyStatsEntity::getStatsDate, statsDate)
        );
        
        if (stats == null) {
            stats = new UserDailyStatsEntity( webId, statsDate);
            stats.setVisitCount(1);
            dailyStatsMapper.insert(stats);
        } else {
            // 使用数据库原子操作更新访问次数，避免并发问题
            UpdateWrapper<UserDailyStatsEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("stats_id", stats.getStatsId())
                         .setSql("visit_count = visit_count + 1");
            dailyStatsMapper.update(null, updateWrapper);
        }
    }
    
    // 日期工具方法
    private Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    private Date getMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    private Date getMonthEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    
    private Date getLastMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    private Date getLastMonthEnd() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    
    private Date getDateStart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    /**
     * 获取小时趋势数据
     */
    private List<VisitStatsVO.TrendDataVO> getHourTrendData( String webId, Date startDate, Date endDate) {
        List<VisitStatsVO.TrendDataVO> result = new ArrayList<>();
        
        // 查询该时间段内的访问记录
        List<UserVisitRecordEntity> records = visitRecordMapper.selectList(
            new LambdaQueryWrapper<UserVisitRecordEntity>()
                .eq(UserVisitRecordEntity::getWebId, webId)
                .ge(UserVisitRecordEntity::getVisitTime, startDate)
                .le(UserVisitRecordEntity::getVisitTime, endDate)
                .orderByAsc(UserVisitRecordEntity::getVisitTime)
        );
        
        // 按小时分组统计
        Map<Integer, List<UserVisitRecordEntity>> hourlyData = records.stream()
            .collect(Collectors.groupingBy(record -> {
                Calendar cal = Calendar.getInstance();
                cal.setTime(record.getVisitTime());
                return cal.get(Calendar.HOUR_OF_DAY);
            }));
        
        // 生成24小时的数据
        for (int hour = 0; hour < 24; hour++) {
            VisitStatsVO.TrendDataVO trendItem = new VisitStatsVO.TrendDataVO();
            trendItem.setTimeLabel(String.format("%02d:00", hour));
            
            List<UserVisitRecordEntity> hourRecords = hourlyData.getOrDefault(hour, new ArrayList<>());
            trendItem.setVisitCount(hourRecords.size());
            trendItem.setUniqueUsers((int) hourRecords.stream()
                .map(UserVisitRecordEntity::getUserId)
                .distinct()
                .count());
            
            int totalDuration = hourRecords.stream()
                .mapToInt(record -> record.getDuration() != null ? record.getDuration() : 0)
                .sum();
            trendItem.setTotalDuration(totalDuration);
            trendItem.setAvgDuration(hourRecords.isEmpty() ? 0.0 : 
                (double) totalDuration / hourRecords.size());
            
            result.add(trendItem);
        }
        
        return result;
    }
    
    /**
     * 获取日趋势数据
     */
    private List<VisitStatsVO.TrendDataVO> getDayTrendData( String webId, Date startDate, Date endDate) {
        List<VisitStatsVO.TrendDataVO> result = new ArrayList<>();
        
        // 查询每日统计数据
        List<UserDailyStatsEntity> dailyStats = dailyStatsMapper.selectList(
            new LambdaQueryWrapper<UserDailyStatsEntity>()
                .eq(UserDailyStatsEntity::getWebId, webId)
                .ge(UserDailyStatsEntity::getStatsDate, startDate)
                .le(UserDailyStatsEntity::getStatsDate, endDate)
                .orderByAsc(UserDailyStatsEntity::getStatsDate)
        );
        
        // 按日期分组
        Map<Date, UserDailyStatsEntity> dailyMap = dailyStats.stream()
            .collect(Collectors.toMap(UserDailyStatsEntity::getStatsDate, stats -> stats));
        
        // 生成日期范围内的数据
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        while (!cal.getTime().after(endDate)) {
            Date currentDate = getDateStart(cal.getTime());
            VisitStatsVO.TrendDataVO trendItem = new VisitStatsVO.TrendDataVO();
            trendItem.setTimeLabel(DateUtil.format(currentDate,"yyyy-MM-dd HH:mm:ss"));
            
            UserDailyStatsEntity dayStats = dailyMap.get(currentDate);
            if (dayStats != null) {
                trendItem.setVisitCount(dayStats.getVisitCount());
                trendItem.setUniqueUsers(1); // 单用户数据
                trendItem.setTotalDuration(dayStats.getTotalDuration());
                trendItem.setAvgDuration(dayStats.getAvgDuration() != null ? 
                    dayStats.getAvgDuration().doubleValue() : 0.0);
            } else {
                trendItem.setVisitCount(0);
                trendItem.setUniqueUsers(0);
                trendItem.setTotalDuration(0);
                trendItem.setAvgDuration(0.0);
            }
            
            result.add(trendItem);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return result;
    }
    
    /**
     * 获取月趋势数据
     */
    private List<VisitStatsVO.TrendDataVO> getMonthTrendData( String webId, Date startDate, Date endDate) {
        List<VisitStatsVO.TrendDataVO> result = new ArrayList<>();
        
        // 查询网站汇总统计数据
        List<UserDailyStatsEntity> dailyStats = dailyStatsMapper.selectList(
                new LambdaQueryWrapper<UserDailyStatsEntity>()
                        .eq(UserDailyStatsEntity::getWebId, webId)
                        .ge(UserDailyStatsEntity::getStatsDate, startDate)
                        .le(UserDailyStatsEntity::getStatsDate, endDate)
                        .orderByAsc(UserDailyStatsEntity::getStatsDate)
        );
        
        // 按月份分组
        Map<String, List<UserDailyStatsEntity>> monthlyMap = dailyStats.stream()
            .collect(Collectors.groupingBy(summary -> {
                Calendar cal = Calendar.getInstance();
                cal.setTime(summary.getStatsDate());
                return String.format("%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
            }));
        
        // 生成月份范围内的数据
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        while (!cal.getTime().after(endDate)) {
            String monthKey = String.format("%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
            VisitStatsVO.TrendDataVO trendItem = new VisitStatsVO.TrendDataVO();
            trendItem.setTimeLabel(monthKey);
            
            List<UserDailyStatsEntity> monthStats = monthlyMap.getOrDefault(monthKey, new ArrayList<>());
            if (!monthStats.isEmpty()) {
                int totalVisits = monthStats.stream()
                    .mapToInt(UserDailyStatsEntity::getVisitCount)
                    .sum();
                int totalDuration = monthStats.stream()
                    .mapToInt(UserDailyStatsEntity::getTotalDuration)
                    .sum();
                
                trendItem.setVisitCount(totalVisits);
                trendItem.setTotalDuration(totalDuration);
                trendItem.setAvgDuration(totalVisits > 0 ? (double) totalDuration / totalVisits : 0.0);
            } else {
                trendItem.setVisitCount(0);
                trendItem.setTotalDuration(0);
                trendItem.setAvgDuration(0.0);
            }
            
            result.add(trendItem);
            
            // 移动到下个月
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }
        
        return result;
    }
}