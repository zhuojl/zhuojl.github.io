package com.zhuojl.study.sort;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 排序工具
 *
 * @author junliang.zhuo
 * @date 2019-05-01 10:32
 */
public class SortUtil {

    public static final Integer[][] args = {{6, 7, 8, 9, 10, 4, 3, 5, 1, 2}, {1, 2, 3, 4, 5}, {5, 4, 3, 2, 1}};

    public static void swap(Integer[] args, int i, int j) {
        if (i == j) {
            return;

        }
        int temp = args[i];
        args[i] = args[j];
        args[j] = temp;
    }

    public static void test(Consumer<Integer[]> consumer) {
        Arrays.stream(args).forEach(arg -> log(consumer, arg));
    }

    private static void log(Consumer<Integer[]> consumer, Integer[] args1) {
        System.out.println(Arrays.toString(args1));
        consumer.accept(args1);
        System.out.println(Arrays.toString(args1));
        System.out.println("complete");
    }
}
