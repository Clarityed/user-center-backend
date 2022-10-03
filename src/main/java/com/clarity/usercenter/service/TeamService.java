package com.clarity.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clarity.usercenter.model.domain.Team;
import com.clarity.usercenter.model.domain.User;
import com.clarity.usercenter.model.dto.TeamQuery;
import com.clarity.usercenter.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 查询队伍列表
     * @param teamQuery 查询条件
     * @param isAdmin 用于判断是否为管理员的值
     * @return 队伍列表
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);
}
