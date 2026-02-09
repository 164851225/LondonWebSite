package com.oddfar.campus.common.model.api.pv;

import lombok.Data;

import java.util.List;

@Data
public class MeterTodayPvResultData {
    public List<String> fail_ps_key_list;
    public List<TodayDevicePointWrap> device_point_list;
}
