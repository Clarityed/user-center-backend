package com.clarity.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clarity.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务操作方法
 *
 * @author Clarity
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2022-08-10 14:26:19
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 用户校验密码
     * @param userCode      用户编码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userCode);


    /**
     * 登录信息
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      请求
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户查询
     *
     * @param username 用户名
     * @param request  请求
     * @return 用户列表
     */
    List<User> searchUsers(String username, HttpServletRequest request);

    /**
     * 删除用户
     *
     * @param id 用户id
     * @return 1 -删除成功 0 -删除失败
     */
    int deleteUser(long id, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser 用户信息
     * @return 数据脱敏的用户
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request 请求
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户 (内存过滤)
     *
     * @param tagNameList 用户要拥有的标签
     * @return 用户列表
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 修改用户
     *
     * @param user 要修改的用户
     * @param loginUser 当前登录用户
     * @return 修改的数据条数
     */
    int updateUser(User user, User loginUser);

    /**
     * 是否为管理员
     *
     * @param request 请求
     * @return false - 代表普通用户, true - 代表管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser 当前登录的用户
     * @return false - 代表普通用户, true - 代表管理员
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取当前登录用户信息
     *
     * @param request 请求
     * @return 当前用户对象
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 分页查询，推荐用户
     *
     * @param pageSize 单页条数
     * @param pageNum 当前是第几页
     * @param request 请求体
     * @return 经过分页的对象
     */
    Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request);
}
