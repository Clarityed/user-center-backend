package com.clarity.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clarity.usercenter.model.domain.User;
import com.clarity.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.clarity.usercenter.constant.UserConstant.SERVER_REDIS_LOCK_KEY;

/**
 * 缓存预热定时任务
 *
 * @author: clarity
 * @date: 2022年09月27日 14:45
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 重点用户，这里不可能是全部用户
    // 要是全部用户肯定要分时间段进行缓存更新
    // 但我们这个系统就指定重点用户缓存预热
    // 以后这里会进行优化的
    private List<Long> mainUserList = Arrays.asList(2L);

    @Scheduled(cron = "0 15 16 * * *")
    public void doCacheRecommendUser() {
        // 使用 Redisson 获得一把锁
        RLock lock = redissonClient.getLock("yupao:precache:docache:lock");
        // 只有一个线程能获得到锁
        try {
            // tryLock 的第二个参数如果填写 -1 表示，启动续锁功能
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("%s%s", SERVER_REDIS_LOCK_KEY,userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    System.out.println("getLock: " + Thread.currentThread().getId());
                    // 写缓存
                    try {
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error");
                    }
                }
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
