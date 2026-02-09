package com.oddfar.campus.common.model.api.pv.his;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 灯具数据响应实体类
 * 用于封装灯具数据接口返回的完整响应数据
 */
@Data
public class MeterHisPvDataResponse {

    public String req_serial_num;
    public String result_code;
    public String result_msg;
    public Map<String, Map<String, List<JSONObject>>> result_data;
}