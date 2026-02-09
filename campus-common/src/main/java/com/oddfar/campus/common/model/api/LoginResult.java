package com.oddfar.campus.common.model.api;

import lombok.Data;

/**
 * 登录结果实体类
 * 用于封装登录成功后返回的token信息
 */
@Data
public class LoginResult {
    
    /**
     * 访问令牌，用于后续接口调用的身份认证
     */
    private String accessToken;
    
    /**
     * 刷新令牌，用于在accessToken过期后刷新获取新的accessToken
     */
    private String refreshToken;
}