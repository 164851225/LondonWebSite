package com.oddfar.campus.framework.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;


import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.domain.entity.DbStatisticsDataEntity;
import com.oddfar.campus.common.domain.entity.LightEnergyEntity;
import com.oddfar.campus.common.domain.entity.PvHisEnergyEntity;
import com.oddfar.campus.common.domain.entity.PvTodayEnergyEntity;
import com.oddfar.campus.common.domain.excel.FillExcelData;
import com.oddfar.campus.common.domain.vo.*;
import com.oddfar.campus.framework.mapper.DbStatisticsDataMapper;
import com.oddfar.campus.framework.mapper.LightEnergyMapper;
import com.oddfar.campus.framework.mapper.PvHisEnergyMapper;
import com.oddfar.campus.framework.mapper.PvTodayEnergyMapper;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LightEnergyServiceImpl {

    @Resource
    private LightEnergyMapper lightEnergyMapper;
    @Resource
    private PvHisEnergyMapper pvHisEnergyMapper;
    @Resource
    private PvTodayEnergyMapper pvTodayEnergyMapper;
    @Autowired
    private DbStatisticsDataMapper dbStatisticsDataMapper;

    public SmartDisplayTotalVo getTotal(SmartDisplayCommonReq param) {
        SmartDisplayTotalVo total = lightEnergyMapper.getTotal(param);
        param.setParkId(null);
        if (total == null) {
            total = new SmartDisplayTotalVo();
            total.setCurrentEnergy(BigDecimal.ZERO);
            total.setSavingEnergy(BigDecimal.ZERO);
            total.setSavingMoney(BigDecimal.ZERO);
        }
        total.setSavingMoney(total.getSavingEnergy().multiply(new BigDecimal(0.64)).setScale(2, BigDecimal.ROUND_HALF_UP));
        return total;
    }


    public SmartDisplayLightAndAcInfoVO lightAndACInfo(SmartDisplayCommonReq param) {
        SmartDisplayTotalVo total = lightEnergyMapper.getTotal(param);
        SmartDisplayLightAndAcInfoVO result = new SmartDisplayLightAndAcInfoVO();
        result.setLightCount(200);
        result.setAcCount(30);
        result.setGatewayCount(10);
        if(total == null){
            result.setLightCurrentEnergy(BigDecimal.ZERO);
            result.setLightSavingEnergy(BigDecimal.ZERO);
        }else {
            result.setLightCurrentEnergy(total.getCurrentEnergy());
            result.setLightSavingEnergy(total.getSavingEnergy());
        }

        return result;
    }


    public SmartDisplayPVTotalVo pvInfo(SmartDisplayCommonReq param) {
        SmartDisplayPVTotalVo result = new SmartDisplayPVTotalVo();
        result.setP83022Energy(BigDecimal.ZERO);
        result.setP83033Energy(BigDecimal.ZERO);
        result.setP83024Energy(BigDecimal.ZERO);
        result.setPower(150);
        result.setSnCount(3);
        List<PvTodayEnergyEntity> pvTodayEnergyEntities = pvTodayEnergyMapper.selectList();
        if (CollUtil.isEmpty(pvTodayEnergyEntities)) {
            return result;
        }
        PvTodayEnergyEntity pvTodayEnergyEntity = pvTodayEnergyEntities.get(0);
        result.setP83022Energy(pvTodayEnergyEntity.getP83022());
        result.setP83033Energy(pvTodayEnergyEntity.getP83033());
        result.setP83024Energy(pvTodayEnergyEntity.getP83024());
        return result;
    }


    public SmartDisplayStatisticChartVO statisticChart(SmartDisplayCommonReq param) {
        //判断startTime和endTime是不是一年的开始和结束时间
        int end = isFirstOrLastDayOfYear(DateUtil.format(param.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
        int start = isFirstOrLastDayOfYear(DateUtil.format(param.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
        SmartDisplayStatisticChartVO result = new SmartDisplayStatisticChartVO();

        //按月聚合数据
        if (start == 1 && end == -1) {
            List<LightEnergyEntity> lightEnergyEntities = lightEnergyMapper.selectList(new LambdaQueryWrapperX<LightEnergyEntity>()
                    .eqIfPresent(LightEnergyEntity::getParkId, param.getParkId())
                    .le(LightEnergyEntity::getChartTime, param.getEndTime())
                    .ge(LightEnergyEntity::getChartTime, param.getStartTime())
                    .orderBy(true, true, LightEnergyEntity::getChartTime)
            );

            List<TimeDataVo> savingEnergyChart = new ArrayList<>();
            List<TimeDataVo> lightEnergyChart = new ArrayList<>();

            if (CollUtil.isNotEmpty(lightEnergyEntities)) {
                savingEnergyChart = lightEnergyEntities.stream().map(item -> {
                    TimeDataVo timeDataVo = new TimeDataVo();
                    timeDataVo.setTime(item.getChartTime());
                    timeDataVo.setValue(item.getSavingEnergy());
                    return timeDataVo;
                }).collect(Collectors.toList());
                lightEnergyChart = lightEnergyEntities.stream().map(item -> {
                    TimeDataVo timeDataVo = new TimeDataVo();
                    timeDataVo.setTime(item.getChartTime());
                    timeDataVo.setValue(item.getCurrentEnergy());
                    return timeDataVo;
                }).collect(Collectors.toList());
            }

            // 补全缺失月份数据
            result.setCurrentEnergyChart(lightEnergyChart);
            result.setSavingEnergyChart(savingEnergyChart);
            result.setLightEnergyChart(lightEnergyChart);

            List<DbStatisticsDataEntity> dbStatisticsDataEntities = dbStatisticsDataMapper.selectList(new LambdaQueryWrapperX<DbStatisticsDataEntity>()
                    .eqIfPresent(DbStatisticsDataEntity::getParkId, param.getParkId())
                    .le(DbStatisticsDataEntity::getStatisticsTime, param.getEndTime())
                    .ge(DbStatisticsDataEntity::getStatisticsTime, param.getStartTime())
                    .orderBy(true, true, DbStatisticsDataEntity::getStatisticsTime)
            );
            List<TimeDataVo> currentEnergyChart = new ArrayList<>(lightEnergyChart);
            List<TimeDataVo> dbStatisticDataList = new ArrayList<>();
            if (CollUtil.isNotEmpty(dbStatisticsDataEntities)) {
                dbStatisticDataList = dbStatisticsDataEntities.stream().map(item -> {
                    TimeDataVo timeDataVo = new TimeDataVo();
                    timeDataVo.setTime(item.getStatisticsTime());
                    timeDataVo.setValue(item.getData());
                    return timeDataVo;
                }).collect(Collectors.toList());
            }

            currentEnergyChart = mergeTimeDataLists(currentEnergyChart, dbStatisticDataList);
            result.setCurrentEnergyChart(currentEnergyChart);
            List<PvHisEnergyEntity> pvHisEnergyEntities = pvHisEnergyMapper.selectList(new LambdaQueryWrapperX<PvHisEnergyEntity>()
                    .le(PvHisEnergyEntity::getTimeStamp, DateUtil.format(param.getEndTime(), "YYYYMMdd"))
                    .ge(PvHisEnergyEntity::getTimeStamp, DateUtil.format(param.getStartTime(), "YYYYMMdd"))
                    .orderBy(true, true, PvHisEnergyEntity::getTimeStamp)
            );


            List<TimeDataVo> pv = new ArrayList<>();

            if (CollUtil.isNotEmpty(pvHisEnergyEntities)) {
                pv = pvHisEnergyEntities.stream().map(item -> {
                    TimeDataVo timeDataVo = new TimeDataVo();
                    try {
                        timeDataVo.setTime(new java.text.SimpleDateFormat("yyyyMMdd").parse(String.valueOf(item.getTimeStamp())));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    timeDataVo.setValue(item.getValue());
                    return timeDataVo;
                }).collect(Collectors.toList());
            }

            result.setPvEnergyChart(pv);
            //把result的数据从每天一个数据改成每月一个数据，值是月的汇总

            result.setPvEnergyChart(
                    fillMissingMonths(
                            result.getPvEnergyChart().stream()
                                    .collect(Collectors.groupingBy(
                                            item -> YearMonth.from(item.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                                            Collectors.reducing(BigDecimal.ZERO, TimeDataVo::getValue, BigDecimal::add)
                                    ))
                                    .entrySet().stream()
                                    .map(entry -> {
                                        TimeDataVo vo = new TimeDataVo();
                                        // 将 YearMonth 转换为 Date，设置为该月第一天
                                        vo.setTime(Date.from(entry.getKey().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                                        vo.setValue(entry.getValue());
                                        return vo;
                                    })
                                    .collect(Collectors.toList()),
                            param.getStartTime(),
                            param.getEndTime()
                    )


            );

            result.setLightEnergyChart(
                    fillMissingMonths(
                            result.getLightEnergyChart().stream()
                                    .collect(Collectors.groupingBy(
                                            item -> YearMonth.from(item.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                                            Collectors.reducing(BigDecimal.ZERO, TimeDataVo::getValue, BigDecimal::add)
                                    ))
                                    .entrySet().stream()
                                    .map(entry -> {
                                        TimeDataVo vo = new TimeDataVo();
                                        // 将 YearMonth 转换为 Date，设置为该月第一天
                                        vo.setTime(Date.from(entry.getKey().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                                        vo.setValue(entry.getValue());
                                        return vo;
                                    })
                                    .collect(Collectors.toList()),
                            param.getStartTime(),
                            param.getEndTime()
                    )


            );

            result.setCurrentEnergyChart(
                    fillMissingMonths(

                            result.getCurrentEnergyChart().stream()
                                    .collect(Collectors.groupingBy(
                                            item -> YearMonth.from(item.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                                            Collectors.reducing(BigDecimal.ZERO, TimeDataVo::getValue, BigDecimal::add)
                                    ))
                                    .entrySet().stream()
                                    .map(entry -> {
                                        TimeDataVo vo = new TimeDataVo();
                                        // 将 YearMonth 转换为 Date，设置为该月第一天
                                        vo.setTime(Date.from(entry.getKey().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                                        vo.setValue(entry.getValue());
                                        return vo;
                                    })
                                    .collect(Collectors.toList()),
                            param.getStartTime(),
                            param.getEndTime()
                    )


            );

            result.setSavingEnergyChart(
                    fillMissingMonths(


                            result.getSavingEnergyChart().stream()
                                    .collect(Collectors.groupingBy(
                                            item -> YearMonth.from(item.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()),
                                            Collectors.reducing(BigDecimal.ZERO, TimeDataVo::getValue, BigDecimal::add)
                                    ))
                                    .entrySet().stream()
                                    .map(entry -> {
                                        TimeDataVo vo = new TimeDataVo();
                                        // 将 YearMonth 转换为 Date，设置为该月第一天
                                        vo.setTime(Date.from(entry.getKey().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                                        vo.setValue(entry.getValue());
                                        return vo;
                                    })
                                    .collect(Collectors.toList()),
                            param.getStartTime(),
                            param.getEndTime()
                    )


            );


        } else {
            List<LightEnergyEntity> lightEnergyEntities = lightEnergyMapper.selectList(new LambdaQueryWrapperX<LightEnergyEntity>()
                    .eqIfPresent(LightEnergyEntity::getParkId, param.getParkId())
                    .le(LightEnergyEntity::getChartTime, param.getEndTime())
                    .ge(LightEnergyEntity::getChartTime, param.getStartTime())
                    .orderBy(true, true, LightEnergyEntity::getChartTime)
            );

            List<TimeDataVo> savingEnergyChart = new ArrayList<>();
            List<TimeDataVo> currentEnergyChart = new ArrayList<>();

            if (CollUtil.isNotEmpty(lightEnergyEntities)) {
                savingEnergyChart = lightEnergyEntities.stream().map(item -> {
                    TimeDataVo timeDataVo = new TimeDataVo();
                    timeDataVo.setTime(item.getChartTime());
                    timeDataVo.setValue(item.getSavingEnergy());
                    return timeDataVo;
                }).collect(Collectors.toList());
                currentEnergyChart = lightEnergyEntities.stream().map(item -> {
                    TimeDataVo timeDataVo = new TimeDataVo();
                    timeDataVo.setTime(item.getChartTime());
                    timeDataVo.setValue(item.getCurrentEnergy());
                    return timeDataVo;
                }).collect(Collectors.toList());
            }

            // 补全缺失日期数据
            savingEnergyChart = fillMissingDays(savingEnergyChart, param.getStartTime(), param.getEndTime());
            currentEnergyChart = fillMissingDays(currentEnergyChart, param.getStartTime(), param.getEndTime());

            result.setCurrentEnergyChart(currentEnergyChart);
            result.setSavingEnergyChart(savingEnergyChart);
            result.setLightEnergyChart(currentEnergyChart);


            List<DbStatisticsDataEntity> dbStatisticsDataEntities = dbStatisticsDataMapper.selectList(new LambdaQueryWrapperX<DbStatisticsDataEntity>()
                    .eqIfPresent(DbStatisticsDataEntity::getParkId, param.getParkId())
                    .le(DbStatisticsDataEntity::getStatisticsTime, param.getEndTime())
                    .ge(DbStatisticsDataEntity::getStatisticsTime, param.getStartTime())
                    .orderBy(true, true, DbStatisticsDataEntity::getStatisticsTime)
            );
            List<TimeDataVo> dbStatisticDataList = new ArrayList<>();
            if (CollUtil.isNotEmpty(dbStatisticsDataEntities)) {
                dbStatisticDataList = dbStatisticsDataEntities.stream().map(item -> {
                    TimeDataVo timeDataVo = new TimeDataVo();
                    timeDataVo.setTime(item.getStatisticsTime());
                    timeDataVo.setValue(item.getData());
                    return timeDataVo;
                }).collect(Collectors.toList());
            }

            currentEnergyChart = mergeTimeDataLists(currentEnergyChart, dbStatisticDataList);
            currentEnergyChart = fillMissingDays(currentEnergyChart, param.getStartTime(), param.getEndTime());
            result.setCurrentEnergyChart(currentEnergyChart);


            List<PvHisEnergyEntity> pvHisEnergyEntities = pvHisEnergyMapper.selectList(new LambdaQueryWrapperX<PvHisEnergyEntity>()
                    .le(PvHisEnergyEntity::getTimeStamp, DateUtil.format(param.getEndTime(), "YYYYMMdd"))
                    .ge(PvHisEnergyEntity::getTimeStamp, DateUtil.format(param.getStartTime(), "YYYYMMdd"))
                    .orderBy(true, true, PvHisEnergyEntity::getTimeStamp)
            );

            List<TimeDataVo> pv = new ArrayList<>();

            if (CollUtil.isNotEmpty(pvHisEnergyEntities)) {
                pv = pvHisEnergyEntities.stream().map(item -> {
                    TimeDataVo timeDataVo = new TimeDataVo();
                    try {
                        timeDataVo.setTime(new java.text.SimpleDateFormat("yyyyMMdd").parse(String.valueOf(item.getTimeStamp())));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    timeDataVo.setValue(item.getValue());
                    return timeDataVo;
                }).collect(Collectors.toList());
            }

            // 补全缺失日期数据
            pv = fillMissingDays(pv, param.getStartTime(), param.getEndTime());

            result.setPvEnergyChart(pv);

        }

        return result;
    }

    /**
     * 补全缺失的日期数据（按天）
     *
     * @param dataList  原始数据列表
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 补全后的数据列表
     */
    private List<TimeDataVo> fillMissingDays(List<TimeDataVo> dataList, Date startDate, Date endDate) {
        // 创建一个包含所有日期的集合
        Map<Date, TimeDataVo> dataMap = new LinkedHashMap<>();

        // 初始化所有日期，值为0
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        while (!cal.getTime().after(endDate)) {
            TimeDataVo vo = new TimeDataVo();
            vo.setTime(cal.getTime());
            vo.setValue(BigDecimal.ZERO);
            dataMap.put(cal.getTime(), vo);

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 用实际数据覆盖默认值
        for (TimeDataVo data : dataList) {
            dataMap.put(data.getTime(), data);
        }

        // 按日期升序排序并返回（小日期在前，大日期在后）
        return dataMap.values().stream()
                .sorted(Comparator.comparing(TimeDataVo::getTime))
                .collect(Collectors.toList());
    }

    /**
     * 补全缺失的月份数据
     *
     * @param dataList  原始数据列表
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 补全后的数据列表
     */
    private List<TimeDataVo> fillMissingMonths(List<TimeDataVo> dataList, Date startDate, Date endDate) {
        // 创建一个包含所有月份的集合
        Map<YearMonth, TimeDataVo> dataMap = new LinkedHashMap<>();

        // 初始化所有月份，值为0
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        YearMonth startYM = YearMonth.from(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        YearMonth endYM = YearMonth.from(endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        YearMonth currentYM = startYM;
        while (!currentYM.isAfter(endYM)) {
            TimeDataVo vo = new TimeDataVo();
            // 设置为该月第一天
            Date monthFirstDay = Date.from(currentYM.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            vo.setTime(monthFirstDay);
            vo.setValue(BigDecimal.ZERO);
            dataMap.put(currentYM, vo);

            // 移动到下一个月
            currentYM = currentYM.plusMonths(1);
        }

        // 用实际数据覆盖默认值
        for (TimeDataVo data : dataList) {
            YearMonth ym = YearMonth.from(data.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            dataMap.put(ym, data);
        }

        // 按日期升序排序并返回（小日期在前，大日期在后）
        return dataMap.values().stream()
                .sorted(Comparator.comparing(vo -> vo.getTime()))
                .collect(Collectors.toList());
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 判断给定时间字符串是否正好是本年的第一天 00:00:00 或最后一天 23:59:59
     *
     * @return 0=都不是；1=第一天；-1=最后一天
     */
    public static int isFirstOrLastDayOfYear(String yyyyMMddHHmmss) {
        LocalDateTime dt = LocalDateTime.parse(yyyyMMddHHmmss, FMT);
        int year = dt.getYear();

        LocalDateTime firstSecond = LocalDate.of(year, 1, 1).atStartOfDay();                 // 当年第一天 00:00:00
        LocalDateTime lastSecond = LocalDate.of(year, 12, 31).atTime(23, 59, 59); // 当年最后一秒

        if (dt.equals(firstSecond)) {
            return 1;
        }
        if (dt.equals(lastSecond)) {
            return -1;
        }
        return 0;
    }

    /**
     * 合并两个时间序列数据列表，相同时间的数据值相加
     *
     * @param list1 第一个时间序列数据列表
     * @param list2 第二个时间序列数据列表
     * @return 合并后的时间序列数据列表
     */
    private List<TimeDataVo> mergeTimeDataLists(List<TimeDataVo> list1, List<TimeDataVo> list2) {
        // 使用LinkedHashMap保持插入顺序
        Map<Date, TimeDataVo> mergedMap = new LinkedHashMap<>();

        // 处理第一个列表
        for (TimeDataVo item : list1) {
            Date time = item.getTime();
            BigDecimal value = item.getValue() != null ? item.getValue() : BigDecimal.ZERO;
            if (mergedMap.containsKey(time)) {
                // 如果已存在相同时间的数据，则相加
                BigDecimal existingValue = mergedMap.get(time).getValue();
                BigDecimal newValue = existingValue.add(value);
                mergedMap.get(time).setValue(newValue);
            } else {
                // 否则直接放入map
                TimeDataVo newItem = new TimeDataVo(time, value);
                mergedMap.put(time, newItem);
            }
        }

        // 处理第二个列表
        for (TimeDataVo item : list2) {
            Date time = item.getTime();
            BigDecimal value = item.getValue() != null ? item.getValue() : BigDecimal.ZERO;
            if (mergedMap.containsKey(time)) {
                // 如果已存在相同时间的数据，则相加
                BigDecimal existingValue = mergedMap.get(time).getValue();
                BigDecimal newValue = existingValue.add(value);
                mergedMap.get(time).setValue(newValue);
            } else {
                // 否则直接放入map
                TimeDataVo newItem = new TimeDataVo(time, value);
                mergedMap.put(time, newItem);
            }
        }

        // 返回按时间排序的列表
        return mergedMap.values().stream()
                .sorted(Comparator.comparing(TimeDataVo::getTime))
                .collect(Collectors.toList());
    }

    public void outputByExcel(SmartDisplayCommonReq param, HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-disposition", "attachment;filename=energy_data.xlsx");

        // 查询数据
        List<LightEnergyEntity> lightEnergyEntities = lightEnergyMapper.selectList(new LambdaQueryWrapperX<LightEnergyEntity>()
                .eqIfPresent(LightEnergyEntity::getParkId, param.getParkId())
                .le(LightEnergyEntity::getChartTime, param.getEndTime())
                .ge(LightEnergyEntity::getChartTime, param.getStartTime())
                .orderBy(true, true, LightEnergyEntity::getChartTime)
        );
        List<TimeDataVo> saveDataList = lightEnergyEntities.stream().map(item -> {
            TimeDataVo timeDataVo = new TimeDataVo();
            timeDataVo.setTime(item.getChartTime());
            timeDataVo.setValue(item.getSavingEnergy());
            return timeDataVo;
        }).collect(Collectors.toList());
        List<TimeDataVo> currentTimeDataList = lightEnergyEntities.stream().map(item -> {
            TimeDataVo timeDataVo = new TimeDataVo();
            timeDataVo.setTime(item.getChartTime());
            timeDataVo.setValue(item.getCurrentEnergy());
            return timeDataVo;
        }).collect(Collectors.toList());

        List<DbStatisticsDataEntity> dbStatisticsDataEntities = dbStatisticsDataMapper.selectList(new LambdaQueryWrapperX<DbStatisticsDataEntity>()
                .eqIfPresent(DbStatisticsDataEntity::getParkId, param.getParkId())
                .le(DbStatisticsDataEntity::getStatisticsTime, param.getEndTime())
                .ge(DbStatisticsDataEntity::getStatisticsTime, param.getStartTime())
                .orderBy(true, true, DbStatisticsDataEntity::getStatisticsTime)
        );

        List<TimeDataVo> dbStatisticsTimeDataList = dbStatisticsDataEntities.stream().map(item -> {
            TimeDataVo timeDataVo = new TimeDataVo();
            timeDataVo.setTime(item.getStatisticsTime());
            timeDataVo.setValue(item.getData());
            return timeDataVo;
        }).collect(Collectors.toList());

        List<TimeDataVo> timeDataVos = mergeTimeDataLists(currentTimeDataList, dbStatisticsTimeDataList);


        saveDataList = fillMissingDays(saveDataList, param.getStartTime(), param.getEndTime());
        timeDataVos = fillMissingDays(timeDataVos, param.getStartTime(), param.getEndTime());

        // 转换数据为Excel格式
        List<FillExcelData.EnergyConsumption> energyConsumptions = convertToEnergyConsumptionList(saveDataList, timeDataVos);

        //energyConsumptions转换成按周聚合，date为周的第一天
        List<FillExcelData.EnergyConsumption> weeklyEnergyConsumptions = aggregateByWeek(energyConsumptions);

        //energyConsumptions转换成按月聚合，date为月的第一天
        List<FillExcelData.EnergyConsumption> monthlyEnergyConsumptions = aggregateByMonth(energyConsumptions);


        List<PvHisEnergyEntity> pvHisEnergyEntities = pvHisEnergyMapper.selectList(new LambdaQueryWrapperX<PvHisEnergyEntity>()
                .le(PvHisEnergyEntity::getTimeStamp, DateUtil.format(param.getEndTime(), "YYYYMMdd"))
                .ge(PvHisEnergyEntity::getTimeStamp, DateUtil.format(param.getStartTime(), "YYYYMMdd"))
                .orderBy(true, true, PvHisEnergyEntity::getTimeStamp)
        );
        List<TimeDataVo> pvTimeDataList = pvHisEnergyEntities.stream().map(item -> {
            TimeDataVo timeDataVo = new TimeDataVo();
            try {
                timeDataVo.setTime(new SimpleDateFormat("yyyyMMdd").parse(String.valueOf(item.getTimeStamp())));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            timeDataVo.setValue(item.getValue());
            return timeDataVo;
        }).collect(Collectors.toList());


        pvTimeDataList = fillMissingDays(pvTimeDataList, param.getStartTime(), param.getEndTime());


        // 转换光伏发电数据为Excel格式
        List<FillExcelData.SolarPower> solarPowers = convertToSolarPowerList(pvTimeDataList);

        // 光伏发电数据按周聚合
        List<FillExcelData.SolarPower> weeklySolarPowers = aggregateSolarPowerByWeek(solarPowers);

        // 光伏发电数据按月聚合
        List<FillExcelData.SolarPower> monthlySolarPowers = aggregateSolarPowerByMonth(solarPowers);


        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
            // 耗能（日）
            WriteSheet dailyEnergySheet = EasyExcel.writerSheet(0, "耗能（日）")
                    .head(FillExcelData.EnergyConsumption.class)
                    .registerWriteHandler(getColumnWidthStyleStrategy())
                    .build();
            excelWriter.write(energyConsumptions, dailyEnergySheet);

            // 耗能（周）
            WriteSheet weeklyEnergySheet = EasyExcel.writerSheet(1, "耗能（周）")
                    .head(FillExcelData.EnergyConsumption.class)
                    .registerWriteHandler(getColumnWidthStyleStrategy())
                    .build();
            excelWriter.write(weeklyEnergyConsumptions, weeklyEnergySheet);

            // 耗能（月）
            WriteSheet monthlyEnergySheet = EasyExcel.writerSheet(2, "耗能（月）")
                    .head(FillExcelData.EnergyConsumption.class)
                    .registerWriteHandler(getColumnWidthStyleStrategy())
                    .build();
            excelWriter.write(monthlyEnergyConsumptions, monthlyEnergySheet);

            // 光伏发电（日）
            WriteSheet dailySolarSheet = EasyExcel.writerSheet(3, "光伏发电（日）")
                    .head(FillExcelData.SolarPower.class)
                    .registerWriteHandler(getColumnWidthStyleStrategy())
                    .build();
            excelWriter.write(solarPowers, dailySolarSheet);

            // 光伏发电（周）
            WriteSheet weeklySolarSheet = EasyExcel.writerSheet(4, "光伏发电（周）")
                    .head(FillExcelData.SolarPower.class)
                    .registerWriteHandler(getColumnWidthStyleStrategy())
                    .build();
            excelWriter.write(weeklySolarPowers, weeklySolarSheet);

            // 光伏发电（月）
            WriteSheet monthlySolarSheet = EasyExcel.writerSheet(5, "光伏发电（月）")
                    .head(FillExcelData.SolarPower.class)
                    .registerWriteHandler(getColumnWidthStyleStrategy())
                    .build();
            excelWriter.write(monthlySolarPowers, monthlySolarSheet);
        }
    }

    /**
     * 获取列宽策略，设置标题列宽为默认的2倍
     *
     * @return CustomColumnWidthStrategy
     */
    private com.alibaba.excel.write.handler.WriteHandler getColumnWidthStyleStrategy() {
        return new com.alibaba.excel.write.handler.SheetWriteHandler() {
            @Override
            public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
                // 在工作表创建前的操作
            }

            @Override
            public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
                org.apache.poi.ss.usermodel.Sheet sheet = writeSheetHolder.getSheet();
                // 设置每列的宽度为默认的2倍
                // 对于EnergyConsumption类和SolarPower类：
                sheet.setColumnWidth(0, 50 * 256); // 日期列宽
                sheet.setColumnWidth(1, 30 * 256); // 度数列宽
                sheet.setColumnWidth(2, 30 * 256); // 节能度数列宽
                
                // 设置header头背景色为白色
                org.apache.poi.ss.usermodel.Workbook workbook = sheet.getWorkbook();
                org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
                headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
                
                // 获取header行（通常是第0行）并应用白色背景
                org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
                if (headerRow != null) {
                    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                        org.apache.poi.ss.usermodel.Cell cell = headerRow.getCell(i);
                        if (cell != null) {
                            cell.setCellStyle(headerStyle);
                        } else {
                            cell = headerRow.createCell(i);
                            cell.setCellStyle(headerStyle);
                        }
                    }
                }
            }

        };
    }

    /**
     * 将TimeDataVo列表转换为EnergyConsumption列表
     *
     * @param hdnsList 节能数据列表 (saveDataList -> hdns)
     * @param nhdsList 能耗数据列表 (currentTimeDataList + dbStatisticsTimeDataList -> nhds)
     * @return EnergyConsumption对象列表
     */
    private List<FillExcelData.EnergyConsumption> convertToEnergyConsumptionList(List<TimeDataVo> hdnsList, List<TimeDataVo> nhdsList) {
        // 创建一个Map，以时间为键，存储节能数据
        Map<Date, BigDecimal> hdnsMap = hdnsList.stream()
                .collect(Collectors.toMap(TimeDataVo::getTime, TimeDataVo::getValue, (a, b) -> a));

        // 创建一个Map，以时间为键，存储能耗数据
        Map<Date, BigDecimal> nhdsMap = nhdsList.stream()
                .collect(Collectors.toMap(TimeDataVo::getTime, TimeDataVo::getValue, (a, b) -> a));

        // 合并两个Map的数据，创建EnergyConsumption对象列表
        Set<Date> allDates = new HashSet<>();
        allDates.addAll(hdnsMap.keySet());
        allDates.addAll(nhdsMap.keySet());

        List<FillExcelData.EnergyConsumption> result = new ArrayList<>();
        for (Date date : allDates) {
            FillExcelData.EnergyConsumption energyConsumption = new FillExcelData.EnergyConsumption();
            energyConsumption.setDate(date);
            energyConsumption.setNhds(nhdsMap.getOrDefault(date, BigDecimal.ZERO)); // nhds 对应能耗度数
            energyConsumption.setJnds(hdnsMap.getOrDefault(date, BigDecimal.ZERO)); // hdns 对应节能度数
            result.add(energyConsumption);
        }

        // 按日期排序
        result.sort(Comparator.comparing(FillExcelData.EnergyConsumption::getDate));
        return result;
    }

    /**
     * 将TimeDataVo列表转换为SolarPower列表
     *
     * @param pvTimeDataList 光伏发电数据列表
     * @return SolarPower对象列表
     */
    private List<FillExcelData.SolarPower> convertToSolarPowerList(List<TimeDataVo> pvTimeDataList) {
        return pvTimeDataList.stream().map(item -> {
            FillExcelData.SolarPower solarPower = new FillExcelData.SolarPower();
            solarPower.setDate(item.getTime());
            solarPower.setFdds(item.getValue()!=null ? item.getValue().divide(new BigDecimal("1000"),2, RoundingMode.HALF_UP) : BigDecimal.ZERO); // fdds 对应发电度数
            return solarPower;
        }).collect(Collectors.toList());
    }

    /**
     * 按周聚合EnergyConsumption数据
     *
     * @param energyConsumptions 原始按日数据列表
     * @return 按周聚合后的数据列表，日期为每周的第一天（周一）
     */
    private List<FillExcelData.EnergyConsumption> aggregateByWeek(List<FillExcelData.EnergyConsumption> energyConsumptions) {
        if (energyConsumptions == null || energyConsumptions.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用TreeMap保持按周分组后的时间顺序
        Map<Date, List<FillExcelData.EnergyConsumption>> weeklyDataMap = new TreeMap<>(
                Comparator.comparing(Date::getTime)
        );

        for (FillExcelData.EnergyConsumption consumption : energyConsumptions) {
            Date date = consumption.getDate();
            // 获取该日期所在周的第一天（周一）
            Date weekStart = getWeekStartDate(date);

            // 将数据归入对应周
            weeklyDataMap.computeIfAbsent(weekStart, k -> new ArrayList<>()).add(consumption);
        }

        // 对每一周的数据进行汇总
        List<FillExcelData.EnergyConsumption> result = new ArrayList<>();
        for (Map.Entry<Date, List<FillExcelData.EnergyConsumption>> entry : weeklyDataMap.entrySet()) {
            Date weekStart = entry.getKey();
            List<FillExcelData.EnergyConsumption> weekDataList = entry.getValue();

            // 计算本周的总能耗和总节能
            BigDecimal totalNhds = weekDataList.stream()
                    .map(FillExcelData.EnergyConsumption::getNhds)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalHdns = weekDataList.stream()
                    .map(FillExcelData.EnergyConsumption::getJnds)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 创建汇总后的EnergyConsumption对象
            FillExcelData.EnergyConsumption aggregatedConsumption = new FillExcelData.EnergyConsumption();
            aggregatedConsumption.setDate(weekStart); // 日期设置为周的第一天
            aggregatedConsumption.setNhds(totalNhds);
            aggregatedConsumption.setJnds(totalHdns);
            result.add(aggregatedConsumption);
        }

        return result;
    }

    /**
     * 获取指定日期所在周的开始日期（周一）
     *
     * @param date 输入日期
     * @return 该周的周一日期
     */
    private Date getWeekStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 设置一周的第一天为周一（在中国，一周通常从周一开始计算）
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        // 设置为这一周的周一
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        // 清除时间部分，只保留日期
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 按周聚合SolarPower数据
     *
     * @param solarPowers 原始按日数据列表
     * @return 按周聚合后的数据列表，日期为每周的第一天（周一）
     */
    private List<FillExcelData.SolarPower> aggregateSolarPowerByWeek(List<FillExcelData.SolarPower> solarPowers) {
        if (solarPowers == null || solarPowers.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用TreeMap保持按周分组后的时间顺序
        Map<Date, List<FillExcelData.SolarPower>> weeklyDataMap = new TreeMap<>(
                Comparator.comparing(Date::getTime)
        );

        for (FillExcelData.SolarPower power : solarPowers) {
            Date date = power.getDate();
            // 获取该日期所在周的第一天（周一）
            Date weekStart = getWeekStartDate(date);

            // 将数据归入对应周
            weeklyDataMap.computeIfAbsent(weekStart, k -> new ArrayList<>()).add(power);
        }

        // 对每一周的数据进行汇总
        List<FillExcelData.SolarPower> result = new ArrayList<>();
        for (Map.Entry<Date, List<FillExcelData.SolarPower>> entry : weeklyDataMap.entrySet()) {
            Date weekStart = entry.getKey();
            List<FillExcelData.SolarPower> weekDataList = entry.getValue();

            // 计算本周的总发电量
            BigDecimal totalFdds = weekDataList.stream()
                    .map(FillExcelData.SolarPower::getFdds)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 创建汇总后的SolarPower对象
            FillExcelData.SolarPower aggregatedPower = new FillExcelData.SolarPower();
            aggregatedPower.setDate(weekStart); // 日期设置为周的第一天
            aggregatedPower.setFdds(totalFdds.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP));
            result.add(aggregatedPower);
        }

        return result;
    }

    /**
     * 按月聚合EnergyConsumption数据
     *
     * @param energyConsumptions 原始按日数据列表
     * @return 按月聚合后的数据列表，日期为每月的第一天
     */
    private List<FillExcelData.EnergyConsumption> aggregateByMonth(List<FillExcelData.EnergyConsumption> energyConsumptions) {
        if (energyConsumptions == null || energyConsumptions.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用TreeMap保持按月分组后的时间顺序
        Map<Date, List<FillExcelData.EnergyConsumption>> monthlyDataMap = new TreeMap<>(
                Comparator.comparing(Date::getTime)
        );

        for (FillExcelData.EnergyConsumption consumption : energyConsumptions) {
            Date date = consumption.getDate();
            // 获取该日期所在月的第一天
            Date monthStart = getMonthStartDate(date);

            // 将数据归入对应月
            monthlyDataMap.computeIfAbsent(monthStart, k -> new ArrayList<>()).add(consumption);
        }

        // 对每一月的数据进行汇总
        List<FillExcelData.EnergyConsumption> result = new ArrayList<>();
        for (Map.Entry<Date, List<FillExcelData.EnergyConsumption>> entry : monthlyDataMap.entrySet()) {
            Date monthStart = entry.getKey();
            List<FillExcelData.EnergyConsumption> monthDataList = entry.getValue();

            // 计算本月的总能耗和总节能
            BigDecimal totalNhds = monthDataList.stream()
                    .map(FillExcelData.EnergyConsumption::getNhds)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalHdns = monthDataList.stream()
                    .map(FillExcelData.EnergyConsumption::getJnds)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 创建汇总后的EnergyConsumption对象
            FillExcelData.EnergyConsumption aggregatedConsumption = new FillExcelData.EnergyConsumption();
            aggregatedConsumption.setDate(monthStart); // 日期设置为月的第一天
            aggregatedConsumption.setNhds(totalNhds);
            aggregatedConsumption.setJnds(totalHdns);
            result.add(aggregatedConsumption);
        }

        return result;
    }

    /**
     * 按月聚合SolarPower数据
     *
     * @param solarPowers 原始按日数据列表
     * @return 按月聚合后的数据列表，日期为每月的第一天
     */
    private List<FillExcelData.SolarPower> aggregateSolarPowerByMonth(List<FillExcelData.SolarPower> solarPowers) {
        if (solarPowers == null || solarPowers.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用TreeMap保持按月分组后的时间顺序
        Map<Date, List<FillExcelData.SolarPower>> monthlyDataMap = new TreeMap<>(
                Comparator.comparing(Date::getTime)
        );

        for (FillExcelData.SolarPower power : solarPowers) {
            Date date = power.getDate();
            // 获取该日期所在月的第一天
            Date monthStart = getMonthStartDate(date);

            // 将数据归入对应月
            monthlyDataMap.computeIfAbsent(monthStart, k -> new ArrayList<>()).add(power);
        }

        // 对每一月的数据进行汇总
        List<FillExcelData.SolarPower> result = new ArrayList<>();
        for (Map.Entry<Date, List<FillExcelData.SolarPower>> entry : monthlyDataMap.entrySet()) {
            Date monthStart = entry.getKey();
            List<FillExcelData.SolarPower> monthDataList = entry.getValue();

            // 计算本月的总发电量
            BigDecimal totalFdds = monthDataList.stream()
                    .map(FillExcelData.SolarPower::getFdds)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 创建汇总后的SolarPower对象
            FillExcelData.SolarPower aggregatedPower = new FillExcelData.SolarPower();
            aggregatedPower.setDate(monthStart); // 日期设置为月的第一天
            aggregatedPower.setFdds(totalFdds.divide(new BigDecimal("1000"),2, RoundingMode.HALF_UP));
            result.add(aggregatedPower);
        }

        return result;
    }

    /**
     * 获取指定日期所在月的第一天
     *
     * @param date 输入日期
     * @return 该月的第一天日期
     */
    private Date getMonthStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 设置为该月的第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        // 清除时间部分，只保留日期
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}


