package org.lpz.usercenter.service;

import org.apache.ibatis.executor.ExecutorException;
import org.junit.jupiter.api.Test;
import org.lpz.usercenter.model.domain.User;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUsersTest {

    @Resource
    UserService userService;

    //自己定义线程池
//    private ExecutorService executorService = new ThreadPoolExecutor(40,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));

    /**
     * 批量插入数据
     */
    @Test
     public void doInsertUsers(){
        final int INSERT_NUM = 100000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<User> list = new ArrayList<>();
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
            list.add(user);
        }
        //20秒 10万
        userService.saveBatch(list,10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发批量插入数据
     */
    @Test
    public void doConcurrencyInsertUsers(){
        final int INSERT_NUM = 100000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        //分十组
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0;i <20;i ++){
            List<User> list = new ArrayList<>();
            while (true) {
                j ++;
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
                list.add(user);
                if (j % 10000 == 0) break;
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreedName:" + Thread.currentThread().getName());
                userService.saveBatch(list,10000);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
