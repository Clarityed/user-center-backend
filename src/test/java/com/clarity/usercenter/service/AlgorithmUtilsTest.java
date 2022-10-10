package com.clarity.usercenter.service;

import com.clarity.usercenter.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * 算法工具类测试
 *
 * @author: clarity
 * @date: 2022年10月09日 14:47
 */
@SpringBootTest
public class AlgorithmUtilsTest {

    @Test
    public void minDistanceTest() {
        String str1 = "clarity是程序员";
        String str2 = "clarity不是程序员";
        String str3 = "clarity是不是程序员呢？";
        int score1 = AlgorithmUtils.minDistance(str1, str2); // 1
        int score2 = AlgorithmUtils.minDistance(str1, str3); // 4
        System.out.println(score1);
        System.out.println(score2);
    }

    @Test
    public void minDistanceTagTest() {
        List<String> tagList1 = Arrays.asList("java", "大一", "男");
        List<String> tagList2 = Arrays.asList("java", "大二", "男");
        List<String> tagList3 = Arrays.asList("java", "大三", "女");
        int score1 = AlgorithmUtils.minDistance(tagList1, tagList2); // 1
        int score2 = AlgorithmUtils.minDistance(tagList1, tagList3); // 2
        System.out.println(score1);
        System.out.println(score2);
    }

}
