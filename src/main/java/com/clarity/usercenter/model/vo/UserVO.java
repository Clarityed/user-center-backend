package com.clarity.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户包装类（脱敏）
 *
 * @author: clarity
 * @date: 2022年10月03日 17:06
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 6570442953422066462L;

    /**
     * id
     */
    // 本来这里是使用 Long 包装类，但是这样可能会出现空指针异常，
    // 改用基本类型
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 个人简介
     */
    private String userProfile;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0 -正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户角色 0 -普通用户 1 -管理员
     */
    private Integer userRole;

    /**
     * 用户编号
     */
    private String userCode;

    /**
     * 用户 json 标签
     */
    private String tags;


}
