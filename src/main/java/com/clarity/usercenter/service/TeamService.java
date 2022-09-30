package com.clarity.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clarity.usercenter.model.domain.Team;
import com.clarity.usercenter.model.domain.User;

/**
* @author Clarity
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2022-09-30 10:05:00
*/
public interface TeamService extends IService<Team> {

    /**
     * 添加队伍
     * @param team 创建的队伍信息
     * @param loginUser 当前登录用户
     * @return 队伍 id
     */
    long addTeam(Team team, User loginUser);

}
