package com.oddfar.campus.common.model.api.pv.his;

import lombok.Data;

import java.util.List;

/**
 * 灯具数据请求实体类
 * 用于封装获取灯具数据接口的请求参数
 */
@Data
public class MeterHisPvDataRequest {
    public String appkey;
    public String token;
    public String data_point;
    public String end_time;
    public String query_type;
    public String start_time;
    public List<String> ps_key_list;
    public String data_type;
    public String order;
}