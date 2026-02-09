package com.oddfar.campus.admin.controller.system;

import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.domain.entity.SysConfigEntity;
import com.oddfar.campus.common.domain.vo.*;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import com.oddfar.campus.framework.service.SysConfigService;
import com.oddfar.campus.framework.service.impl.LightEnergyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 配置管理
 */
@RestController
@RequestMapping("/smart/display")
public class SmartDisplayController {
    @Autowired
    private LightEnergyServiceImpl lightEnergyService;

    @PostMapping(value = "/total")
    public R total(@RequestBody SmartDisplayCommonReq param) {
        SmartDisplayTotalVo total = lightEnergyService.getTotal(param);
//        total.setCurrentEnergy(BigDecimal.TEN);
//        total.setSavingEnergy(BigDecimal.ONE);
//        total.setSavingMoney(BigDecimal.ONE);

        return R.ok().put(total);
    }


    @PostMapping(value = "/lightAndACInfo")
    public R lightAndACInfo(@RequestBody SmartDisplayCommonReq param) {
        SmartDisplayLightAndAcInfoVO total = lightEnergyService.lightAndACInfo(param);

//        total.setLightCount(10);
//        total.setAcCount(10);
//        total.setLightCurrentEnergy(BigDecimal.ONE);
//        total.setLightSavingEnergy(BigDecimal.ONE);
//        total.setGatewayCount(10);
        return R.ok().put(total);
    }


    @PostMapping(value = "/pvInfo")
    public R pvInfo(@RequestBody SmartDisplayCommonReq param) {
        SmartDisplayPVTotalVo total = lightEnergyService.pvInfo(param);
        return R.ok().put(total);
    }


    @PostMapping(value = "/statisticChart")
    public R statisticChart(@RequestBody SmartDisplayCommonReq param) {
        SmartDisplayStatisticChartVO total = lightEnergyService.statisticChart(param);
        //total mock数据

//        // 生成最近20天的日期列表
//        List<TimeDataVo> savingEnergyChart = new ArrayList<>();
//        List<TimeDataVo> currentEnergyChart = new ArrayList<>();
//        List<TimeDataVo> lightEnergyChart = new ArrayList<>();
//        List<TimeDataVo> pvEnergyChart = new ArrayList<>();
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//        for (int i = 19; i >= 0; i--) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.DAY_OF_MONTH, -i);
//            Date date = calendar.getTime();
//
//            // savingEnergyChart
//            TimeDataVo savingVo = new TimeDataVo();
//            savingVo.setTime(date);
//            savingVo.setValue(new BigDecimal(String.format("%.1f", 1.0 + Math.random() * 5.0)));
//            savingEnergyChart.add(savingVo);
//
//            // currentEnergyChart
//            TimeDataVo currentVo = new TimeDataVo();
//            currentVo.setTime(date);
//            currentVo.setValue(new BigDecimal(String.format("%.1f", 10.0 + Math.random() * 15.0)));
//            currentEnergyChart.add(currentVo);
//
//            // lightEnergyChart
//            TimeDataVo lightVo = new TimeDataVo();
//            lightVo.setTime(date);
//            lightVo.setValue(new BigDecimal(String.format("%.1f", 5.0 + Math.random() * 10.0)));
//            lightEnergyChart.add(lightVo);
//
//            // pvEnergyChart
//            TimeDataVo pvVo = new TimeDataVo();
//            pvVo.setTime(date);
//            pvVo.setValue(new BigDecimal(String.format("%.1f", 0.0 + Math.random() * 8.0)));
//            pvEnergyChart.add(pvVo);
//        }
//
//        total.setSavingEnergyChart(savingEnergyChart);
//        total.setCurrentEnergyChart(currentEnergyChart);
//        total.setLightEnergyChart(lightEnergyChart);
//        total.setPvEnergyChart(pvEnergyChart);


        return R.ok().put(total);
    }



    @PostMapping(value = "/output")
    public R output(@RequestBody SmartDisplayCommonReq param,HttpServletResponse response) throws IOException {
        lightEnergyService.outputByExcel( param,response);
        return R.ok();
    }
}