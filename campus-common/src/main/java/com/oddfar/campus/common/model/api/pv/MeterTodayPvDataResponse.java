package com.oddfar.campus.common.model.api.pv;

import lombok.Data;

/**
 * 灯具数据响应实体类
 * 用于封装灯具数据接口返回的完整响应数据
 */
@Data
public class MeterTodayPvDataResponse {

    public String req_serial_num;
    public String result_code;
    public String result_msg;
    public MeterTodayPvResultData result_data;
}