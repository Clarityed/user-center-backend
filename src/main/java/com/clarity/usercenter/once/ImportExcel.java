package com.clarity.usercenter.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * 导出 Excel
 *
 * @author: clarity
 * @date: 2022年09月15日 20:07
 */
public class ImportExcel {

    /**
     * 读取数据
     */
    public static void main(String[] args) {

        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "E:\\IDEAproject\\user-center\\src\\main\\resources\\excel\\user.xls";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
        readByListener(fileName);
        synchronousRead(fileName);
    }

    /**
     * 监听器
     * @param fileName 数据文件
     */
    public static void readByListener(String fileName) {
        EasyExcel.read(fileName, UserData.class, new UserTableListener()).sheet().doRead();
    }

    /**
     * 监听器
     * @param fileName 数据文件
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<UserData> totalDataList = EasyExcel.read(fileName).head(UserData.class).sheet().doReadSync();
        for (UserData userData : totalDataList) {
            System.out.println(userData);
        }
    }

}
