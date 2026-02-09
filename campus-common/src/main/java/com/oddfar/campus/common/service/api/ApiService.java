package com.oddfar.campus.common.service.api;

import com.alibaba.fastjson2.JSONObject;
import com.oddfar.campus.common.model.api.LoginResult;
import com.oddfar.campus.common.model.api.MeterDataRequest;
import com.oddfar.campus.common.model.api.MeterDataResponse;
import com.oddfar.campus.common.model.api.db.ElectricChartDataResponse;
import com.oddfar.campus.common.model.api.db.ElectricDayTotalDataResponse;
import com.oddfar.campus.common.model.api.pv.MeterTodayPvDataRequest;
import com.oddfar.campus.common.model.api.pv.MeterTodayPvDataResponse;
import com.oddfar.campus.common.model.api.pv.ResultData;
import com.oddfar.campus.common.model.api.pv.his.MeterHisPvDataRequest;
import com.oddfar.campus.common.model.api.pv.his.MeterHisPvDataResponse;

import java.util.List;

/**
 * API接口调用服务接口
 * 定义API服务的公共方法
 */
public interface ApiService {
    
    /**
     * 用户登录
     * @param account 用户名
     * @param password 密码
     * @return 登录结果
     */
    LoginResult login(String account, String password);

    ResultData loginForPv();

    /**
     * 获取灯具数据
     * @param request 灯具数据请求参数
     * @param account 用户名（用于获取访问令牌）
     * @return 灯具数据响应
     */
    MeterDataResponse getMeterData(MeterDataRequest request, String accessToken);

    MeterTodayPvDataResponse getTodayPvMeterData(MeterTodayPvDataRequest request, String accessToken);

    MeterHisPvDataResponse getHisPvMeterData(MeterHisPvDataRequest request, String accessToken);

    List<ElectricChartDataResponse> getDbMeterData(JSONObject request);
    /**
     * 清除用户的访问令牌
     * @param account 用户名
     */
    void clearAccessToken(String account);
    
    /**
     * 刷新访问令牌
     * @param account 用户名
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshAccessToken(String account, String refreshToken);
}