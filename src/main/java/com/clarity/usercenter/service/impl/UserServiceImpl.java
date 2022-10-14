package com.clarity.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clarity.usercenter.common.ErrorCode;
import com.clarity.usercenter.exception.BusinessException;
import com.clarity.usercenter.model.domain.User;
import com.clarity.usercenter.service.UserService;
import com.clarity.usercenter.mapper.UserMapper;
import com.clarity.usercenter.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.clarity.usercenter.constant.UserConstant.*;

/**
 * @author Clarity
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2022-08-10 14:26:19
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    // 引入 redis
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 盐值混淆密码
     */
    private static final String SALT = "clarity";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userCode) {
        // 1. 校验 前后端都要进行账号密码校验，因为用户是可以绕过前端，直接访问后台的
        // 检查用户账号、密码和校验密码是否为非空，这里可以导入 common utils 工具类来实现判断
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数为空");
        }
        // 账号不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户账号过短");
        }
        // 密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户密码过短");
        }
        // 用户编码不能超过5位
        if (userCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户编码过长");
        }
        // 账号不能包含特殊字符
        String validPattern = "[ _`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号不能包含特殊字符");
        }
        // 密码和校验密码是否相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次密码输入不一致");
        }
        // 账户不能重复，把对数据库的操作放在这里是为了提高性能，因为如果账号密码不符合规范，那么就必须要查询数据库。
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户账号重复");
        }
        // 用户编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userCode", userCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户编码重复");
        }
        // 2. 加密，这里用简单的单向加密，采用Spring提供的加密工具

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setUserCode(userCode);
        int insertResult = userMapper.insert(user);
        if (0 == insertResult) {
            throw new BusinessException(ErrorCode.INSERT_ERROR, "数据提交到数据库失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验 前后端都要进行账号密码校验，因为用户是可以绕过前端，直接访问后台的
        // 检查用户账号、密码和校验密码是否为非空，这里可以导入 common utils 工具类来实现判断
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码都不能为空");
        }
        // 账号不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户账号过短");
        }
        // 密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户密码过短");
        }
        // 账号不能包含特殊字符
        String validPattern = "[ _`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号不能包含特殊字符");
        }

        // 2. 加密，这里用简单的单向加密，采用Spring提供的加密工具
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("User login failed, userAccount cannot match userPassword.");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码错误");
        }

        // 3. 数据脱敏
        User safetyUser = getSafetyUser(user);

        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    @Override
    public List<User> searchUsers(String username, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求为空");
        }
        // 只有管理员可以查询用户
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "你不是管理员，无法使用查询功能");
        }
        // StringUtils.isNotBlank判断是空字符串，还空格字符串，还null，如果是返回true
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        // 注意以下这段代码是 java 8的写法，不会要去补。
        return userList.stream().map(user -> this.getSafetyUser(user)).collect(Collectors.toList());
    }

    @Override
    public int deleteUser(long id, HttpServletRequest request) {
        // 只能管理员可以删除用户
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "你不是管理员，无法使用删除功能");
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不合法");
        }
        return userMapper.deleteById(id);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 如果在这里直接抛出异常的话，后面不是用户调用该方法会立刻停止操作
/*        if (user == null || user.getUserRole() != ADMIN_ROLE) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在或者用户无权限访问");
        }*/
        return user == null || user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User loginUser) {
/*        if (loginUser == null || loginUser.getUserRole() != ADMIN_ROLE) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在或者用户无权限访问");
        }*/
        return loginUser == null || loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 请求不能为空
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }

    @Override
    public Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        // 判断用户是否登录，登录的话，才能查看推荐用户
        User loginUser = this.getLoginUser(request);
        // 不同的用户缓存不同，为他们设置各自唯一的 key
        String redisKey = String.format("%s%s", SERVER_REDIS_LOCK_KEY, loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 逻辑上应该是先读缓存，如果有缓存那么直接返回数据
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return userPage;
        }
        // 如果缓存没有数据，则查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // pageNum 表示当前是第几页，从第 1 页开始，pageSize 表示每也的长度。
        userPage = userMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        // 最后记得把数据写入 redis 缓存，使用 try catch 有原因，如果 redis 出问题程序依旧会往下执行。
        try {
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error");
        }
        return userPage;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        queryWrapper.last("limit 100000");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> loginUserTagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
/*        // 用户列表的下表 => 相似度
        SortedMap<Integer, Long> indexDistanceMap = new TreeMap<>();*/
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 该用户无标签
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算得分
            long distance = AlgorithmUtils.minDistance(loginUserTagList, userTagsList);
            list.add(new Pair<>(user, distance));
        }
        // 按照编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 按积分低到高的顺序排序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        /*List<Integer> maxDistanceIndexList = indexDistanceMap.keySet().stream().limit(num).collect(Collectors.toList());
        List<User> safetyUserList = maxDistanceIndexList.stream().map(index -> getSafetyUser(userList.get(index))).collect(Collectors.toList());*/
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, List<User>> safetyUserIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(safetyUserIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 数据脱敏
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "脱敏用户不存在");
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserCode(originUser.getUserCode());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setUserProfile(originUser.getUserProfile());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        // 标签不能为空，如果为空，抛出异常，因为为空的话，会查出所有数据
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 根据内存进行查询
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        // parallelStream() 并发处理流，用了公共的线程池，有很多不确定的因素，可能已经被其他的功能占用了全部线程
        // 面试可能会问的地方
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            // 防止查出用户标签为空，导致下面 Set 集合出现空指针异常，未创建集合对象
            /*if (tagsStr == null) {
                return false;
            }*/
            // 得出单个用户的标签集合
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
            // 也可这样判断集合是否为空
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
            // 要进行数据脱敏
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, User loginUser) {
        if (user == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // todo 补充校验，如果用户没有传任何更新的值，就直接报错，不用执行 update 语句
        // 要修改的用户 id 不可能为 0，对于小于 0 的抛出异常。
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 如果是管理员可以修改任意用户信息
        // 如果是用户可以修改自己的用户信息
        if ( !isAdmin(loginUser) && user.getId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 根据 id 在数据库中查找用户,判断该用户是否存在
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 最后更新用户
        return userMapper.updateById(user);
    }

    /**
     * 根据标签搜索用户 (SQL 查询版)
     *
     * @param tagNameList 用户要拥有的标签
     * @return 用户列表
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        // 标签不能为空，如果为空，抛出异常，因为为空的话，会查出所有数据
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 根据 SQL 进行查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and '%Python%'
        // 通过 QueryWrapper 类提供的 like 方法链式拼接条件
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




