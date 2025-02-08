package org.lpz.usercenter.service;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lpz.usercenter.mapper.UserMapper;
import org.lpz.usercenter.model.domain.User;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * 测试
 * @author lpz
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;

    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("lpz");
        user.setUserAccount("lpz");
        user.setAvatarUrl("");
        user.setUserPassword("");
        user.setGender(0);
        user.setPhone("");
        user.setEmail("");
        user.setUserStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        int save = userMapper.insert(user);
        Assertions.assertEquals(1,save); //断言，期待结果是1，比较期待结果与实际结果是否一致
        System.out.println(user.getId());
    }

    /**
     * 用户注册单元测试
     */
    @Test
    void userRegister() {
        String userAccount = "";
        String userPassword = "12345678";
        String checkPassword = "12345678";
        String planetCode = "1";
        long result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result); //空
        userAccount = "lp";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);//账户长度不小于4
        userPassword = "123";
        checkPassword = "123";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);//密码长度不下于8
        userAccount = "lpz";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);//账户不能重复
        userAccount = "lpz..";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);//账户不包含特殊字符
        userPassword = "12345678";
        checkPassword = "12345679";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);//两次密码相同
        userAccount = "admin";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertTrue(result > 0); //成功插入

    }

    @Test
    void searchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java","python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assertions.assertNotNull(userList);
    }
}