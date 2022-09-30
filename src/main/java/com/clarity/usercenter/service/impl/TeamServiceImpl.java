package com.clarity.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clarity.usercenter.common.ErrorCode;
import com.clarity.usercenter.exception.BusinessException;
import com.clarity.usercenter.model.domain.Team;
import com.clarity.usercenter.mapper.TeamMapper;
import com.clarity.usercenter.model.domain.User;
import com.clarity.usercenter.model.domain.UserTeam;
import com.clarity.usercenter.model.enums.TeamStatusEnum;
import com.clarity.usercenter.service.TeamService;
import com.clarity.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
* @author Clarity
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2022-09-30 10:05:00
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserTeamService userTeamService;

    @Override
    // 该方法开启事务，要么都成功提交，要么都失败
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录不允许创建队伍");
        }
        final long userId = loginUser.getId();
        // 3 校验信息
        //  3.1 队伍人数 > 1，且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍人数不符合要求");
        }
        //  3.2 队伍标题 <= 20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍标题不符合要求");
        }
        /*if (teamName.length() == 0 || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍标题不符合要求");
        }*/
        //  3.3 描述 <= 512
        String teamDescription = team.getDescription();
        if (StringUtils.isNotBlank(teamDescription) && teamDescription.length() > 512) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "描述不符合要求");
        }
        //  3.4 status 是否公开（int）不传默认为 0 （公开），这里我们去定义一个枚举类来进行判断，当前队伍的状态
        Integer teamStatus = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamStatus);
        if (teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍状态不符合要求");
        }
        //  3.5 如果 status 加密状态，一定要有密码，且密码 <= 32
        String teamPassword = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(teamPassword) || teamPassword.length() > 32) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "密码不符合要求");
            }
        }
        //  3.6 超时时间 > 当前时间
        Date teamExpireTime = team.getExpireTime();
        if (new Date().after(teamExpireTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍过期时间在当前时间之前");
        }
        //  3.7 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍创建数量已达当前系统支持的最大值");
        }
        // 4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        team.setCreateTime(new Date());
        team.setUpdateTime(new Date());
        int result = teamMapper.insert(team);
        Long teamId = team.getId();
        if (result == 0 || teamId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍插入失败");
        }
        // 5. 插入用户到队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        userTeam.setCreateTime(new Date());
        userTeam.setUpdateTime(new Date());
        boolean userTeamResult = userTeamService.save(userTeam);
        if (!userTeamResult) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "创建队伍失败");
        }
        return teamId;
    }
}




