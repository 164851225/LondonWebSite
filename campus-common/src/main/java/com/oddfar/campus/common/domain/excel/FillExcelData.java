package com.oddfar.campus.common.domain.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class FillExcelData {
    // POJO for 耗能 sheets (日、周、月)
    @Data
    public static class EnergyConsumption {
        @ExcelProperty( index = 0, value = "日期")
        private Date date;       // 日期
        @ExcelProperty( index = 1, value = "能耗度数")
        private BigDecimal nhds; // 能耗度数
        @ExcelProperty( index = 2, value = "节能度数")
        private BigDecimal jnds;      // 节能度数

    }

    // POJO for 光伏发电 sheets (日、周、月)
    @Data
    public static class SolarPower {
        @ExcelProperty( index = 0, value = "日期")
        private Date date;       // 日期
        @ExcelProperty( index = 1, value = "发电度数")
        private BigDecimal fdds; // 发电度数
    }

    public static void main(String[] args) throws IOException {
        // 模板文件路径（请替换成实际路径）
        ClassPathResource templateResource = new ClassPathResource("template/energy_template.xlsx");

        // 输出文件路径
        String outputFileName = "能耗数据_填充后_" + System.currentTimeMillis() + ".xlsx";

        // 准备各 sheet 数据（用 List<Map> 方式最通用，key 对应占位符）
        List<Map<String, Object>> dailyConsumptionData  = generateMapData("consumption");
        List<Map<String, Object>> weeklyConsumptionData = generateMapData("consumption");
        List<Map<String, Object>> monthlyConsumptionData = generateMapData("consumption");

        List<Map<String, Object>> dailySolarData  = generateMapData("generation");
        List<Map<String, Object>> weeklySolarData = generateMapData("generation");
        List<Map<String, Object>> monthlySolarData = generateMapData("generation");

        try (ExcelWriter excelWriter = EasyExcel.write(outputFileName)
                .withTemplate(templateResource.getInputStream())
                .build()) {

            // 耗能（日） - sheet 索引从 0 开始
            WriteSheet sheet0 = EasyExcel.writerSheet(0).build();
            excelWriter.fill(dailyConsumptionData, sheet0);

            // 耗能（周）
            WriteSheet sheet1 = EasyExcel.writerSheet(1).build();
            excelWriter.fill(weeklyConsumptionData, sheet1);

            // 耗能（月）
            WriteSheet sheet2 = EasyExcel.writerSheet(2).build();
            excelWriter.fill(monthlyConsumptionData, sheet2);

            // 光伏发电（日）
            WriteSheet sheet3 = EasyExcel.writerSheet(3).build();
            excelWriter.fill(dailySolarData, sheet3);

            // 光伏发电（周）
            WriteSheet sheet4 = EasyExcel.writerSheet(4).build();
            excelWriter.fill(weeklySolarData, sheet4);

            // 光伏发电（月）
            WriteSheet sheet5 = EasyExcel.writerSheet(5).build();
            excelWriter.fill(monthlySolarData, sheet5);

            // 如果有单个单元格的固定值，也可以额外 fill 一个 Map
            // Map<String, Object> headMap = new HashMap<>();
            // headMap.put("reportDate", new Date());
            // excelWriter.fill(headMap, EasyExcel.writerSheet(0).build());
        }

        System.out.println("使用模板填充完成，文件生成：" + outputFileName);
    }

    // 生成示例数据 - 耗能类型
    private static List<Map<String, Object>> generateMapData(String type) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {  // 模拟 10 行数据
            Map<String, Object> map = new HashMap<>();
            map.put("date", new Date());

            if ("consumption".equals(type)) {
                map.put("consumption", 100.0 + i * 5);
                map.put("saving", 15.0 + i * 3);
            } else {
                map.put("generation", 50.0 + i * 8);
            }
            list.add(map);
        }
        return list;
    }
}

