package org.lpz.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.lpz.usercenter.model.domain.User;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test(){

        //操作redisson就和操作本地集合一样

        //list 数据保存在本地的JVM内存中
        List<String> list = new ArrayList<>();
        list.add("lpz");
        System.out.println(list.get(0));

        //redisson 数据保存在redis的内存中
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("lpz");
        System.out.println(rList.get(0));

        //map、stack都同理
    }

    @Test
    void testWatchDog(){
        RLock rLock = redissonClient.getLock("yupao:precachejob:docache:lock");
        try {
            //只有一个线程能获得锁
            if (rLock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                System.out.println("getLock:" + Thread.currentThread().getId());
                Thread.sleep(30000);
            }

        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }finally {
            //只能释放自己的锁
            if (rLock.isHeldByCurrentThread()){
                System.out.println("unlock:" + Thread.currentThread().getId());
                rLock.unlock();
            }
        }

    }
}
