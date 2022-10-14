package com.clarity.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clarity.usercenter.common.BaseResponse;
import com.clarity.usercenter.common.ErrorCode;
import com.clarity.usercenter.exception.BusinessException;
import com.clarity.usercenter.model.domain.User;
import com.clarity.usercenter.model.request.UserLoginRequest;
import com.clarity.usercenter.model.request.UserRegisterRequest;
import com.clarity.usercenter.service.UserService;
import com.clarity.usercenter.utils.ResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.clarity.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author: clarity
 * @date: 2022年08月11日 10:48
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173/", "http://localhost:8000/"}, allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    // 此处@RequestBody的意义是：使得前端的请求的数据会去，对于此注册请求体类
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 为了规范我们这里要继续参数的校验，但这里并不是业务的校验（业务越少越好）
        if (userRegisterRequest == null) {
//            return ResultUtils.error(ErrorCode.PARAM_ERROR);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userCode = userRegisterRequest.getUserCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    // 此处@RequestBody的意义是：使得前端的请求的数据会去，对于此注册请求体类
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 为了规范我们这里要继续参数的校验，但这里并不是业务的校验（业务越少越好）
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        int i = userService.userLogout(request);
        return ResultUtils.success(i);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrent(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            // 发现问题：前端的全局入口文件，在程序启动时会最先启动，里面定义了获取当前用户信息的方法，那么就会执行该方法，
            // 结果是用户信息为空，那么就会抛出异常，返回给前端，但是按照响应的方式返回，前端会误认为是有用户存在，然后直接跳转到主页，这是一个bug，待修改。
            /*throw new BusinessException(ErrorCode.PARAM_ERROR);*/
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 之后可能要优化，可能用户会被封号。
        Long id = currentUser.getId();
        // todo 校验用户是否合法
        User user = userService.getById(id);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        List<User> list = userService.searchUsers(username, request);
        return ResultUtils.success(list);
    }

    // @RequestParam 初步理解为关闭 SpringBoot 的错误信息返回，采用我们自己的错误信息返回
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        Page<User> userPage = userService.recommendUsers(pageSize, pageNum, request);
        return ResultUtils.success(userPage);
    }

    @DeleteMapping("/")
    public BaseResponse<Integer> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (id <= 0) {
            return null;
        }
        int i = userService.deleteUser(id, request);
        return ResultUtils.success(i);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 单个用户匹配
     * @param num 要展示的用户数
     * @param request 请求体
     * @return 用户列表
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }

}
