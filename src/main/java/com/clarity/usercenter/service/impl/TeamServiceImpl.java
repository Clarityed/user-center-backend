package com.clarity.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clarity.usercenter.common.ErrorCode;
import com.clarity.usercenter.exception.BusinessException;
import com.clarity.usercenter.model.domain.Team;
import com.clarity.usercenter.mapper.TeamMapper;
import com.clarity.usercenter.model.domain.User;
import com.clarity.usercenter.model.domain.UserTeam;
import com.clarity.usercenter.model.dto.TeamQuery;
import com.clarity.usercenter.model.enums.TeamStatusEnum;
import com.clarity.usercenter.model.request.TeamJoinRequest;
import com.clarity.usercenter.model.request.TeamQuitRequest;
import com.clarity.usercenter.model.request.TeamUpdateRequest;
import com.clarity.usercenter.model.vo.TeamUserVO;
import com.clarity.usercenter.model.vo.UserVO;
import com.clarity.usercenter.service.TeamService;
import com.clarity.usercenter.service.UserService;
import com.clarity.usercenter.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Resource
    private UserService userService;

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

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        // 1. 从请求参数中取出队伍名称等条件，如果存在则作为查询条件。
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            // 3. 可以通过某个关键词同时对名称和描述查询
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQuery.getName();
            // 判断字符串存在，且不能为空字符串
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            // 查询最大人数
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            // 根据创建人来查询队伍
            Long userId = teamQuery.getUserId();
            if (userId != null && maxNum > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据队伍状态查询队伍
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            if (teamStatusEnum == null) {
                teamStatusEnum = TeamStatusEnum.PUBLIC;
            }
            // 4. 只有管理员才能查看加密还有非公开的房间
            if (!isAdmin && !teamStatusEnum.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", teamStatusEnum.getValue());
        }
        // 2. 不展示已过期的队伍，如果队伍未设置过期时间也是能查询出来的
        // 下面这行代码的意思就算，查询队伍过期时间大于当前时间或者队伍没有设置过期时间，也就算永久保留的队伍
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        // 5. 我们这里使用关联查询创建人，写 SQL 自己有时间了自己实现
        // 如果有多张表建议还是使用 写 SQL 来来查询，因为关联查询多张表，然后数据量有大，性能会很差
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            // 得到创建人用户的信息
            User user = userService.getById(userId);
            // 用于存放要传给前端的队伍信息
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 用户信息脱敏，也就是复制用户信息到封装类中，与上面的一样
            // 有可能这个用户信息是不存在的
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        // 1. 判断请求参数是否为空
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 2. 查询队伍是否存在
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team oldTeam = teamMapper.selectById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 3. 只有管理员或者队伍的创建者可以修改
        if (oldTeam.getId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 5. 如果队伍状态改为加密，必须要有密码
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "加密类型队伍，密码不能为空");
            }
        }
        // 6. 更新队伍成功
        Team newTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, newTeam);
        return this.updateById(newTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        // 老样子判断对象是否为空
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        // 队伍 id 不能为空，或者小于等于 0
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 2.1 队伍必须存在
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 2.2 未过期的队伍
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍已过期");
        }
        // 4. 禁止加入私有队伍
        Integer teamStatus = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamStatus);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "禁止加入私有队伍");
        }
        // 5. 如果加入的队伍是加密的，必须密码匹配才可以
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "密码错误");
            }
        }
        // 1. 用户最多加入 5 个队伍
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (hasJoinNum >= 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "最多创建和加入 5 个队伍");
        }
        // 2.3 只能加入未满队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (teamHasJoinNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍人数已满");
        }
        // 3. 不能重复加入已加入的队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamQueryWrapper.eq("userId", userId);
        long hasJoinTeam = userTeamService.count(userTeamQueryWrapper);
        if (hasJoinTeam > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "已加入该队伍");
        }
        // 6. 新增 队伍-用户 关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        userTeam.setCreateTime(new Date());
        userTeam.setUpdateTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        // 2. 校验请求参数
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 3. 校验队伍是否存在
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 4. 校验我是否已经加入队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        long userId = loginUser.getId();
        queryWrapper.eq("teamId", teamId);
        queryWrapper.eq("userId", userId);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "您还没加入该队伍");
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        long teamHasJoinNum = userTeamService.count(queryWrapper);
        // 5.1 如果队伍只剩下一个人
        if (teamHasJoinNum == 1) {
            // 解散队伍，包括删除队伍信息，和队伍加入记录
            int teamDeleteById = teamMapper.deleteById(teamId);
            if (teamDeleteById == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍删除失败");
            }
            boolean userTeamRemoveById = userTeamService.remove(queryWrapper);
            if (!userTeamRemoveById) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍关系删除失败");
            }
        } else { // 还有其他人的情况下
            // 如果是队长退出队伍，权限转移给第二个早加入的用户 ——先来后到
            if (userId == team.getUserId()) {
                // 查询已加入队伍的所有用户和加入时间，我们这里使用 id 来判断谁先加入队伍
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId", teamId);
                queryWrapper.last("order by id limit 2");
                List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
                if (userTeamList == null || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                // 更新队伍队长 id
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                int result = teamMapper.updateById(updateTeam);
                if (result == 0) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队长更新失败");
                }
                // 删除前任队长信息
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId", teamId);
                queryWrapper.eq("userId", userId);
                boolean removeUserTeam = userTeamService.remove(queryWrapper);
                if (!removeUserTeam) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除前任队长关系记录失败");
                }
            } else {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId", teamId);
                queryWrapper.eq("userId", userId);
                boolean removeUserTeam = userTeamService.remove(queryWrapper);
                if (!removeUserTeam) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除前任队长关系记录失败");
                }
            }
        }
        return true;
    }
}




