package org.lpz.usercenter.once;
import java.util.Date;

import org.lpz.usercenter.mapper.UserMapper;
import org.lpz.usercenter.model.domain.User;
import org.lpz.usercenter.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {

    //注入UserService
    @Resource
    UserMapper userMapper;

    /**
     * 批量插入用户
     */
    public void doInsertUsers(){
        final int INSERT_NUM = 1000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假数据");
            user.setUserAccount("11111");
            user.setAvatarUrl("https://gw.alipayobjects.com/zos/bmw-prod/598d14af-4f1c-497d-b579-5ac42cd4dd1f/k7bjua9c_w132_h130.png");
            user.setUserPassword("12345678");
            user.setGender(0);
            user.setPhone("121324564");
            user.setEmail("21211231@qq.com");
            user.setIsDelete(0);
            user.setUserRole(0);
            user.setPlanetCode("1111");
            user.setTags("[]");
            user.setProfile("");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
