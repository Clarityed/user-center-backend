package com.clarity.usercenter.service;
import java.util.Date;

import com.clarity.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * redis 测试类
 *
 * @author: clarity
 * @date: 2022年09月26日 14:55
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("clarityStr", "zwz");
        valueOperations.set("clarityInt", 1);
        valueOperations.set("clarityDouble", 1.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("clarity");
        valueOperations.set("clarityObject", user);
        Object clarityStr = valueOperations.get("clarityStr");
        Assertions.assertTrue("zwz".equals((String) clarityStr));
        Object clarityInt = valueOperations.get("clarityInt");
        Assertions.assertTrue(1 == (Integer) clarityInt);
        Object clarityDouble = valueOperations.get("clarityDouble");
        Assertions.assertTrue(1.0 == (Double) clarityDouble);
        System.out.println(valueOperations.get("clarityObject"));
    }
}
