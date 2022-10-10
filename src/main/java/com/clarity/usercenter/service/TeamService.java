package com.clarity.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.clarity.usercenter.model.domain.Team;
import com.clarity.usercenter.model.domain.User;
import com.clarity.usercenter.model.dto.TeamQuery;
import com.clarity.usercenter.model.request.TeamDeleteRequest;
import com.clarity.usercenter.model.request.TeamJoinRequest;
import com.clarity.usercenter.model.request.TeamQuitRequest;
import com.clarity.usercenter.model.request.TeamUpdateRequest;
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

    /**
     * 更新队伍
     * @param teamUpdateRequest 队伍要更新参数的对象
     * @param loginUser 当前登录用户
     * @return 0 -更新失败 1 -更新成功
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 用户加入队伍
     * @param teamJoinRequest 加入队伍所需的参数
     * @param loginUser 当前登录用户
     * @return 0 -加入失败 1 -加入成功
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 用户退出队伍
     * @param teamQuitRequest 退出队伍所需的参数
     * @param loginUser 当前登录用户
     * @return 0 -退出失败 1 -退出成功
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 队长解散（删除）队伍
     * @param teamDeleteRequest 解散队伍所需的参数
     * @param loginUser 当前登录用户
     * @return 0 -解散失败 1 -解散成功
     */
    boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, User loginUser);

    /**
     * 判断当前用户是否已加入队伍
     * @param teamList 全部队伍
     * @param request 请求参数
     * @return 经过判断的全部队伍，属性已修改
     */
    List<TeamUserVO> isUserJoinTeam(List<TeamUserVO> teamList, HttpServletRequest request);
}
