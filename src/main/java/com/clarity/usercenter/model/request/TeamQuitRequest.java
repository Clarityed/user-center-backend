package com.clarity.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 退出队伍参数请求体
 *
 * @author: clarity
 * @date: 2022年10月07日 15:36
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -1760591668190270488L;

    /**
     * 队伍 id
     */
    private Long teamId;

}
