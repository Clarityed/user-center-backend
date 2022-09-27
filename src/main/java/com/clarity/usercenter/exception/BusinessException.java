package com.clarity.usercenter.exception;

import com.clarity.usercenter.common.ErrorCode;
import lombok.Data;

/**
 * 自定义异常类
 *
 * @author: scott
 * @date: 2022年08月16日 10:43
 */

@Data
public class BusinessException extends RuntimeException {

    private int code;

    private String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
