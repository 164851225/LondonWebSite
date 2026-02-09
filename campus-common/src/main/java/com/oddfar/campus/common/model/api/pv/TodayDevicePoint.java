package com.oddfar.campus.common.model.api.pv;

import lombok.Data;

@Data
public class TodayDevicePoint {
    public String device_name;
    public Integer dev_fault_status;
    public String ps_key;
    public String device_sn;
    public Integer dev_status;
    public String ps_id;
    public String communication_dev_sn;
    public String p83024;
    public String uuid;
    public String p83022;
    public String p83033;
    public Long device_time;

}
