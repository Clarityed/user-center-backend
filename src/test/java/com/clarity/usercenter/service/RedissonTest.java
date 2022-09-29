package com.clarity.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clarity.usercenter.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.clarity.usercenter.constant.UserConstant.SERVER_REDIS_LOCK_KEY;

/**
 * 测试 Redisson 是否成功，和 Redisson 与 本地集合使用上的相似
 *
 * @author: clarity
 * @date: 2022年09月28日 19:26
 */
@SpringBootTest
@Slf4j
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test() {
        // list，数据存储在本地 JVM 内存中
        List<String> list = new ArrayList<>();
        list.add("clarity");
        System.out.println("list: " + list.get(0));

        // 数据存储在 Redis 的内存中
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("clarityR");
        System.out.println(rList.get(0));
    }

    @Test
    public void testWatchDog() {
        // 使用 Redisson 获得一把锁
        RLock lock = redissonClient.getLock("yupao:precache:docache:lock");
        // 只有一个线程能获得到锁
        try {
            // tryLock 的第二个参数如果填写 -1 表示，启动续锁功能
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                Thread.sleep(50000);
            } else {
                System.out.println("该服务器这次不运行此方法！！！");
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser: ", e);
        } finally {
            // 注意一定要在 finally 里写释放锁的语句，因为这样程序就算在 try 里面出现异常报错，也能够正常释放锁，不会造成死锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}
