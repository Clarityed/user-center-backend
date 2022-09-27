package com.clarity.usercenter.exception;

import com.clarity.usercenter.common.BaseResponse;
import com.clarity.usercenter.common.ErrorCode;
import com.clarity.usercenter.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author: clarity
 * @date: 2022年08月16日 11:25
 */

// Spring AOP的一个功能
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 这方法只去捕获BusinessException
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e) {
        log.error("businessException" + e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        // 全局捕获系统内部异常，并进行记录，不用每个地方抛异常都去捕获一下。简称：集中记录
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }

}
