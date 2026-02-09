package com.oddfar.campus.common.model.api;

import lombok.Data;

/**
 * 登录响应实体类
 * 用于封装登录接口返回的完整响应数据
 */
@Data
public class LoginResponse {
    
    /**
     * 响应状态码，200表示成功
     */
    private Integer code;
    
    /**
     * 响应类型，success表示成功
     */
    private String type;
    
    /**
     * 响应消息，成功时为空字符串
     */
    private String message;
    
    /**
     * 登录结果数据
     */
    private LoginResult result;
    
    /**
     * 额外数据，暂未使用时为null
     */
    private Object extras;
    
    /**
     * 响应时间
     */
    private String time;
}