package com.clarity.usercenter.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户中心数据对象
 *
 * @author: clarity
 * @date: 2022年09月15日 19:49
 */
@Data
public class UserData {

    /**
     * id
     */
    @ExcelProperty("id")
    private Long id;
    /**
     * 用户昵称
     */
    @ExcelProperty("username")
    private String username;
    /**
     * 账号
     */
    @ExcelProperty("userAccount")
    private String userAccount;

}
