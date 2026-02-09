package com.oddfar.campus.common.service.api.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.oddfar.campus.common.model.api.*;
import com.oddfar.campus.common.model.api.db.ElectricChartDataResponse;
import com.oddfar.campus.common.model.api.db.ElectricDayTotalDataResponse;
import com.oddfar.campus.common.model.api.pv.*;
import com.oddfar.campus.common.model.api.pv.his.MeterHisPvDataRequest;
import com.oddfar.campus.common.model.api.pv.his.MeterHisPvDataResponse;
import com.oddfar.campus.common.service.api.ApiService;
import com.oddfar.campus.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API接口调用服务实现类
 * 封装对外部API的调用，包括登录和获取灯具数据
 */
@Service
public class ApiServiceImpl implements ApiService {
    
    private static final Logger log = LoggerFactory.getLogger(ApiServiceImpl.class);
    
    // API基础URL
    private static final String API_BASE_URL = "http://s8.widelinker.net/adminApi8/api";
    
    // 登录接口URL
    private static final String LOGIN_URL = API_BASE_URL + "/sysAuth/loginByLiot";

    private static final String PV_LOGIN_URL = "https://gateway.isolarcloud.com/openapi/login";

    // 灯具数据接口URL
    private static final String METER_DATA_URL = "https://s8.widelinker.net/adminApi8/api/dataEnergy/getEnergyChartData";
    private static final String PV_TODAY_METER_DATA_URL = "https://gateway.isolarcloud.com/openapi/getDeviceRealTimeData";
    private static final String PV_HIS_METER_DATA_URL = "https://gateway.isolarcloud.com/openapi/getDevicePointsDayMonthYearDataList";

    //电表能耗
//    private static final String DB_ENERGY_URL = "http://s8.widelinker.net/adminApi8/api/dataElectricMeter/getElectricDayTotalData";
    private static final String DB_ENERGY_URL = "https://s8.widelinker.net/adminApi8/api/dataElectricMeter/getElectricChartData";
    // 缓存的访问令牌
    private static final ConcurrentHashMap<String, String> ACCESS_TOKEN_CACHE = new ConcurrentHashMap<>();
    
    @Override
    public LoginResult login(String account, String password) {
        LoginResult result = new LoginResult();
        result.setAccessToken("");
        return  result;
//        try {
//            // 创建登录请求对象
//            LoginRequest request = new LoginRequest();
//            request.setAccount(account);
//            request.setPassword(password);
//            request.setCodeId(0);
//            request.setCode("string");
//            request.setLoginMode(1);
//
//            // 发送POST请求
//            String responseJson = HttpUtils.sendPostJson(LOGIN_URL, request);
//
//            // 解析响应
//            LoginResponse response = JSONObject.parseObject(responseJson, LoginResponse.class);
//
//            if (response != null && response.getCode() == 200 && response.getResult() != null) {
//                // 缓存访问令牌，以用户名为key
//                LoginResult result = response.getResult();
//                ACCESS_TOKEN_CACHE.put(account, result.getAccessToken());
//                log.info("用户登录成功: {}", account);
//                return result;
//            } else {
//                log.error("用户登录失败: {}, 响应: {}", account, responseJson);
//                throw new RuntimeException("登录失败: " + (response != null ? response.getMessage() : "未知错误"));
//            }
//        } catch (Exception e) {
//            log.error("登录请求异常: {}", e.getMessage(), e);
//            throw new RuntimeException("登录请求异常: " + e.getMessage(), e);
//        }
    }

    @Override
    public ResultData loginForPv() {
        try {
            // 创建登录请求对象
            PvLoginRequest request = new PvLoginRequest();
            request.setAppkey("653D19EFBA5E957EEADAD6262086A52F");
            request.setUser_account("15365071503");
            request.setUser_password("DhZdMt1qPNpT");

            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type",  "application/json");
            headers.put("sys_code",  "901");
            headers.put("x-access-key",  "wb798nc8vs86br38zc7xbj78a2ifdrde");

            // 发送带认证头的POST请求
            String responseJson = HttpUtils.sendPostWithHeadersMy(PV_LOGIN_URL, JSONObject.toJSONString(request), headers);

            // 解析响应
            LoginPvResponse response = JSONObject.parseObject(responseJson, LoginPvResponse.class);

            if (response != null && "1".equals(response.getResult_code())) {
                // 缓存访问令牌，以用户名为key
                return response.getResult_data();
            } else {
                log.error("responseJson: {}",  responseJson);
                throw new RuntimeException("登录失败: " + (response != null ? response : "未知错误"));
            }
        } catch (Exception e) {
            log.error("登录请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("登录请求异常: " + e.getMessage(), e);
        }
    }

    @Override
    public MeterDataResponse getMeterData(MeterDataRequest request, String accessToken) {
        try {
            // 获取访问令牌
            if (accessToken == null) {
                throw new RuntimeException("未找到有效的访问令牌，请先登录");
            }
            
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization",  accessToken);
            
            // 发送带认证头的POST请求
            String responseJson = HttpUtils.sendPostWithHeadersMy(METER_DATA_URL, JSONObject.toJSONString(request), headers);
            
            // 解析响应
            MeterDataResponse response = JSONObject.parseObject(responseJson, MeterDataResponse.class);
            
            if (response != null && response.getCode() == 200) {
                log.info("获取灯具数据成功，园区ID: {}", request.getParkId());
                return response;
            } else {
                log.error("获取灯具数据失败: {}", responseJson);
                throw new RuntimeException("获取灯具数据失败: " + (response != null ? response.getMessage() : "未知错误"));
            }
        } catch (Exception e) {
            log.error("获取灯具数据请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取灯具数据请求异常: " + e.getMessage(), e);
        }
    }


    @Override
    public MeterTodayPvDataResponse getTodayPvMeterData(MeterTodayPvDataRequest request, String accessToken) {
        try {
            // 获取访问令牌
            if (accessToken == null) {
                throw new RuntimeException("未找到有效的访问令牌，请先登录");
            }

            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type",  "application/json");
            headers.put("sys_code",  "901");
            headers.put("x-access-key",  "wb798nc8vs86br38zc7xbj78a2ifdrde");

            String param = "{\n" +
                    "    \"appkey\": \"653D19EFBA5E957EEADAD6262086A52F\",\n" +
                    "    \"token\": \""+accessToken+"\",\n" +
                    "    \"device_type\":\"11\",\n" +
                    "    \"is_get_point_dict\":\"1\",\n" +
                    "    \"point_id_list\": [\"83022\",\"83024\",\"83033\"],\n" +
                    "    \"ps_key_list\": [\"2181093_11_0_0\"]\n" +
                    "}\n";
            // 发送带认证头的POST请求
            String responseJson = HttpUtils.sendPostWithHeadersMy(PV_TODAY_METER_DATA_URL, param, headers);

            // 解析响应
            MeterTodayPvDataResponse response = JSONObject.parseObject(responseJson, MeterTodayPvDataResponse.class);

            if (response != null && "1".equals(response.getResult_code())) {
                log.info("getTodayPvMeterData，responseJson: {}", responseJson);
                return response;
            } else {
                log.error("获取灯具数据失败: {}", responseJson);
                throw new RuntimeException("获取灯具数据失败: " + (response != null ? responseJson : "未知错误"));
            }
        } catch (Exception e) {
            log.error("获取灯具数据请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取灯具数据请求异常: " + e.getMessage(), e);
        }
    }


    @Override
    public MeterHisPvDataResponse getHisPvMeterData(MeterHisPvDataRequest request, String accessToken) {
        try {
            // 获取访问令牌
            if (accessToken == null) {
                throw new RuntimeException("未找到有效的访问令牌，请先登录");
            }

            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type",  "application/json");
            headers.put("sys_code",  "901");
            headers.put("x-access-key",  "wb798nc8vs86br38zc7xbj78a2ifdrde");

            // 发送带认证头的POST请求
            String responseJson = HttpUtils.sendPostWithHeadersMy(PV_HIS_METER_DATA_URL, JSONObject.toJSONString(request), headers);

            // 解析响应
            MeterHisPvDataResponse response = JSONObject.parseObject(responseJson, MeterHisPvDataResponse.class);

            if (response != null && "1".equals(response.getResult_code())) {
                log.info("MeterHisPvDataResponse，response: {}", responseJson);
                return response;
            } else {
                log.error("获取灯具数据失败: {}", responseJson);
                throw new RuntimeException("获取灯具数据失败: " + (response != null ? responseJson : "未知错误"));
            }
        } catch (Exception e) {
            log.error("获取灯具数据请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取灯具数据请求异常: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ElectricChartDataResponse> getDbMeterData(JSONObject request) {
        try {
//            // 获取访问令牌
//            if (accessToken == null) {
//                throw new RuntimeException("未找到有效的访问令牌，请先登录");
//            }

            // 设置请求头
            Map<String, String> headers = new HashMap<>();
//            headers.put("Content-Type",  "application/json");
//            headers.put("sys_code",  "901");
//            headers.put("x-access-key",  "wb798nc8vs86br38zc7xbj78a2ifdrde");

            // 发送带认证头的POST请求
            String responseStr = HttpUtils.sendPostWithHeadersMy(DB_ENERGY_URL, JSONObject.toJSONString(request), headers);

            JSONObject responseJson = JSONObject.parseObject(responseStr);

            if(responseJson.getIntValue("code")!=200){
                log.error("获取数据失败: {}", responseJson);
                throw new RuntimeException("获取数据失败: " + responseJson.getString("message"));
            }

            String jsonString = responseJson.getJSONArray("result").toJSONString();
            // 解析响应
            List<ElectricChartDataResponse> response = JSONArray.parseArray(jsonString, ElectricChartDataResponse.class);
            return response;
        } catch (Exception e) {
            log.error("获取灯具数据请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取灯具数据请求异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取访问令牌
     * @param account 用户名
     * @return 访问令牌
     */
    private String getAccessToken(String account) {
        return ACCESS_TOKEN_CACHE.get(account);
    }
    
    @Override
    public void clearAccessToken(String account) {
        ACCESS_TOKEN_CACHE.remove(account);
        log.info("已清除用户的访问令牌: {}", account);
    }
    
    @Override
    public String refreshAccessToken(String account, String refreshToken) {
        // 这里可以实现刷新令牌的逻辑
        // 目前简单实现为重新登录
        // 注意：实际应用中应该调用专门的刷新令牌接口
        log.warn("刷新令牌功能未完全实现，将重新登录");
        // 实际使用时需要传入正确的用户名和密码
        throw new UnsupportedOperationException("刷新令牌功能需要进一步实现");
    }
}