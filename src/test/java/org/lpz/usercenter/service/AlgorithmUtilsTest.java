package org.lpz.usercenter.service;

import org.junit.jupiter.api.Test;
import org.lpz.usercenter.utils.AlgorithmUtils;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * 算法测试
 */
@SpringBootTest
public class AlgorithmUtilsTest {

    /**
     * 最短编辑距离
     */
    @Test
     void test(){
       String str1 = "lpz123";
       String str2 = "lpz111";
       String str3 = "lpz222";
       String str4 = "lpz444";

        int i = AlgorithmUtils.minDistance(str1, str2);
        int i1 = AlgorithmUtils.minDistance(str3, str4);
        System.out.println(i);
        System.out.println(i1);
    }

    /**
     * 最短编辑距离（标签）
     */
    @Test
    void testTags(){
        List<String> list1 = Arrays.asList("Java","大一","男");
        List<String> list2 = Arrays.asList("Java","大二","男");
        List<String> list3 = Arrays.asList("Java","大一","男");
        List<String> list4 = Arrays.asList("Python","大一","女");
        int i = AlgorithmUtils.minDistance(list1, list2);
        int i1 = AlgorithmUtils.minDistance(list3, list4);
        System.out.println(i);
        System.out.println(i1);
    }
}
