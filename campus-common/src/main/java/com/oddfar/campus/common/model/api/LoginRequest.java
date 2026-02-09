package com.oddfar.campus.common.model.api;

import lombok.Data;

/**
 * 登录请求实体类
 * 用于封装登录接口的请求参数
 */
@Data
public class LoginRequest {
    
    /**
     * 用户名
     */
    private String account;
    
    /**
     * 密码（已加密）
     */
    private String password;
    
    /**
     * 验证码ID，暂未使用时设为0
     */
    private Integer codeId = 0;
    
    /**
     * 验证码，暂未使用时设为"string"
     */
    private String code = "string";
    
    /**
     * 登录模式，1表示默认登录方式
     */
    private Integer loginMode = 1;
}