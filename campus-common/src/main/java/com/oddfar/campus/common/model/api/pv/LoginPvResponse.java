package com.oddfar.campus.common.model.api.pv;

import com.oddfar.campus.common.model.api.LoginResult;
import lombok.Data;

/**
 * 登录响应实体类
 * 用于封装登录接口返回的完整响应数据
 */
@Data
public class LoginPvResponse {

    private String req_serial_num;
    private String result_code;
    private String result_msg;
    private ResultData result_data;
}