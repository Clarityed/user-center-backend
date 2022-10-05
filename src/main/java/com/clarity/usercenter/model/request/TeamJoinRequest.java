package com.clarity.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 加入队伍参数接收类
 *
 * @author: clarity
 * @date: 2022年10月04日 16:27
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -2866968384730037126L;

    /**
     * 队伍 id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}
