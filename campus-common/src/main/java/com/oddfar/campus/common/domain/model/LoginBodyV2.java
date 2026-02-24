package com.oddfar.campus.common.domain.model;

import lombok.Data;

/**
 * 用户登录对象
 *
 * @author ruoyi
 */
@Data
public class LoginBodyV2 {

    private String oldPassword;


    private String newPassword;

}
