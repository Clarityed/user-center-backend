package com.clarity.usercenter.once;
import java.util.Date;

import com.clarity.usercenter.mapper.UserMapper;
import com.clarity.usercenter.model.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * 导入数据
 *
 * @author: clarity
 * @date: 2022年09月22日 17:15
 */
@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    // 谨慎使用，不用时马上注释掉，防止下次从开服务，自动运行，只用于测试环境
    // @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假用户");
            user.setUserAccount("fakeUser");
            user.setAvatarUrl("https://img2.huashi6.com/images/resource/thumbnail/2022/03/10/271_22109410029.jpg?imageMogr2/quality/75/interlace/1/thumbnail/700x/gravity/North/crop/700x794/format/webp");
            user.setGender(0);
            user.setUserPassword("123456789");
            user.setUserProfile("我是模拟测试数据");
            user.setPhone("12345678910");
            user.setEmail("12345678910@qq.com");
            user.setUserStatus(0);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setUserRole(0);
            user.setUserCode("11111111");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
