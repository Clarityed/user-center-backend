package com.clarity.usercenter.constant;

public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    // ------ 权限 ------

    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;

    /**
     * 服务器在 Redis 上的 lock
     */
    String SERVER_REDIS_LOCK_KEY = "yupao:user:recommend:";

}
