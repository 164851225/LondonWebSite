package com.oddfar.campus.common.model.api.pv;

import lombok.Data;

/**
 * 登录请求实体类
 * 用于封装登录接口的请求参数
 */
@Data
public class PvLoginRequest {
    
    /**
     * 用户名
     */
    private String appkey;
    
    /**
     * 密码（已加密）
     */
    private String user_account;

    private String user_password;

}