package com.clarity.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    // 本来这里是使用 Long 包装类，但是这样可能会出现空指针异常，
    // 改用基本类型
    @TableId(type = IdType.AUTO)
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
     * 密码
     */
    private String userPassword;

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
     * 是否删除 0 -正常 1 -删除
     */
    @TableLogic
    private Integer isDelete;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}