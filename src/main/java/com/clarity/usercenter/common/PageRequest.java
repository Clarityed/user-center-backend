package com.clarity.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数类
 *
 * @author: clarity
 * @date: 2022年09月30日 11:13
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 6953844489756732606L;

    /**
     * 每页的显示的记录条数
     */
    protected int pageSize = 10;

    /**
     * 当前是第几页
     */
    protected int pageNum = 1;

}
