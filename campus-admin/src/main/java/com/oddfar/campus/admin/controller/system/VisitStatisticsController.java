package com.oddfar.campus.admin.controller.system;

import com.oddfar.campus.common.annotation.Log;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.domain.dto.HeartbeatDTO;
import com.oddfar.campus.common.domain.dto.VisitRecordDTO;
import com.oddfar.campus.common.domain.dto.VisitStatsQueryDTO;
import com.oddfar.campus.common.domain.entity.UserVisitRecordEntity;
import com.oddfar.campus.common.domain.vo.VisitStatsVO;
import com.oddfar.campus.common.enums.BusinessStatus;
import com.oddfar.campus.framework.service.VisitStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/system/visit")
@Slf4j
public class VisitStatisticsController {

    @Resource
    private VisitStatisticsService visitStatisticsService;

    /**
     * 记录用户访问
     * @param dto 访问记录请求参数
     * @return 访问记录ID
     */
    @PostMapping("/record")
    @PreAuthorize("isAuthenticated()")
    public R recordVisit(@RequestBody VisitRecordDTO dto) {
        try {
            UserVisitRecordEntity record = visitStatisticsService.recordVisit( dto.getWebId(), dto.getPageUrl(), dto.getPageTitle());
            return R.ok("访问记录成功", record.getVisitId());
        } catch (Exception e) {
            log.error("记录访问异常", e);
            return R.error("访问记录失败: " + e.getMessage());
        }
    }

    /**
     * 心跳检测更新停留时长
     * @param dto 心跳检测请求参数
     * @return 操作结果
     */
    @PostMapping("/heartbeat")
    @PreAuthorize("isAuthenticated()")
    public R heartbeat(@RequestBody HeartbeatDTO dto) {
        try {
            visitStatisticsService.updateDuration(dto.getVisitId(), dto.getDuration());
            return R.ok("心跳更新成功");
        } catch (Exception e) {
            log.error("心跳更新异常", e);
            return R.error("心跳更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户今日访问统计
     * @param dto 统计查询请求参数
     * @return 今日访问统计数据
     */
    @PostMapping("/today-stats")
    @PreAuthorize("isAuthenticated()")
    public R getTodayStats(@RequestBody VisitStatsQueryDTO dto) {
        try {
            VisitStatsVO stats = visitStatisticsService.getUserTodayStats( dto.getWebId());
            return R.ok(stats);
        } catch (Exception e) {
            log.error("获取今日统计异常", e);
            return R.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户平均停留时间
     * @param dto 统计查询请求参数
     * @return 平均停留时间(秒)
     */
    @PostMapping("/avg-duration")
    public R getAvgDuration(@RequestBody VisitStatsQueryDTO dto) {
        try {
            BigDecimal avgDuration = visitStatisticsService.getUserAvgDuration( dto.getWebId(), dto.getStartDate(), dto.getEndDate());
            return R.ok(avgDuration);
        } catch (Exception e) {
            log.error("获取平均时长异常", e);
            return R.error("获取平均时长失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户月度访问统计
     * @param dto 统计查询请求参数
     * @return 月度访问统计数据
     */
    @PostMapping("/monthly-stats")
    @PreAuthorize("isAuthenticated()")
    public R getMonthlyStats(@RequestBody VisitStatsQueryDTO dto) {
        try {
            VisitStatsVO stats = visitStatisticsService.getMonthlyVisitStats( dto.getWebId());
            return R.ok(stats);
        } catch (Exception e) {
            log.error("获取月度统计异常", e);
            return R.error("获取月度统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取访问趋势数据
     * @param dto 统计查询请求参数
     * @return 访问趋势数据
     */
    @PostMapping("/trend")
    @PreAuthorize("isAuthenticated()")
    public R getVisitTrend(@RequestBody VisitStatsQueryDTO dto) {
        try {
            List<VisitStatsVO.TrendDataVO> trendData = visitStatisticsService.getVisitTrend(
                 
                dto.getWebId(), 
                dto.getPeriodType(), 
                dto.getStartDate(), 
                dto.getEndDate()
            );
            return R.ok(trendData);
        } catch (Exception e) {
            log.error("获取趋势数据异常", e);
            return R.error("获取趋势数据失败: " + e.getMessage());
        }
    }
}