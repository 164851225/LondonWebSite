package com.oddfar.campus.common.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SmartDisplayStatisticChartVO {

    private List<TimeDataVo> savingEnergyChart;
    private List<TimeDataVo> currentEnergyChart;

    private List<TimeDataVo> lightEnergyChart;

    private List<TimeDataVo> pvEnergyChart;


}
