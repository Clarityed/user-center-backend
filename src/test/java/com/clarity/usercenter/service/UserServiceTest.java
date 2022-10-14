package com.clarity.usercenter.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.clarity.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;



/**
 * 用户服务测试
 *
 * @author Clarity
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("clarity");
        user.setUserAccount("123");
        user.setAvatarUrl("E://123//2313//d.png@942w_1332h_progressive.jpg");
        user.setGender(0);
        user.setUserPassword("123456");
        user.setPhone("123");
        user.setEmail("456");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    public void userRegister() {
        String userAccount = "clarity";
        String userPassword = "123456789";
        String checkPassword = "123456789";
        String userCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
        Assertions.assertEquals(-1, result);
        userAccount = "clarity@";
        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
        Assertions.assertEquals(-1, result);
        userAccount = "";
        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
        Assertions.assertEquals(-1, result);
        checkPassword = "12345678910";
        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
        Assertions.assertEquals(-1, result);
        userPassword = "123456";
        checkPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
        Assertions.assertEquals(-1, result);
        userAccount = "231";
        userPassword = "123456789";
        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
        Assertions.assertEquals(-1, result);
        userAccount = "clarity2121";
        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
        Assertions.assertTrue(result > 0);
    }

    @Test
    public void testSearchUsersByTags() {
        List<String> tagNameList = Arrays.asList("Java", "Python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assert.assertNotNull(userList);
    }

}