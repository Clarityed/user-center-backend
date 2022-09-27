package com.clarity.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author: clarity
 * @date: 2022年08月11日 10:41
 */

@Data
public class UserRegisterRequest implements Serializable {
    // Serializable 防止序列化时发生冲突
    private static final long serialVersionUID = -4930465615931640561L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String userCode;

}
