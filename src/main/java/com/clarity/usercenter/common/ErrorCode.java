package com.clarity.usercenter.common;

/**
 * 通用错误类
 *
 * @author clarity
 */
public enum ErrorCode {

    // 要使用自动生成文档的话，这些枚举类的每一项都要加上注解
    SUCCESS(0, "ok", ""),
    PARAM_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    FORBIDDEN(40301, "禁止操作", ""),
    SYSTEM_ERROR(50000, "系统内部异常", ""),
    INSERT_ERROR(40002, "数据插入失败", "");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

}
