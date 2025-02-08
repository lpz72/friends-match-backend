package org.lpz.usercenter.service;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lpz.usercenter.model.domain.User;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("lpzString","dog");
        valueOperations.set("lpzInt",1);
        valueOperations.set("lpzDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("lpz");
        valueOperations.set("lpzUser",user);

        //查
        Object lpz = valueOperations.get("lpzString");
        Assertions.assertTrue("dog".equals(lpz));
        lpz = valueOperations.get("lpzInt");
        Assertions.assertTrue(1 == (Integer) lpz);
        lpz = valueOperations.get("lpzDouble");
        Assertions.assertTrue(2.0 == (Double) lpz);

        //删
        redisTemplate.delete("lpzString");

    }
}
