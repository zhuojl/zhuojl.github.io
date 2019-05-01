package com.zhuojl.study.sort;

import java.util.Arrays;

/**
 * 冒泡
 *
 * @author junliang.zhuo
 * @date 2019-05-01 10:32
 */
public class Bubble {

    public static void main(String[] args) {
        SortUtil.test(Bubble::test);
    }

    public static void test(Integer[] args) {
        System.out.println(Arrays.toString(args));
        for (int i = 0; i < args.length - 1; i++) {
            for (int j = 0; j < args.length - 1 - i; j++) {
                if (args[j] > args[j + 1]) {
                    SortUtil.swap(args, j, j + 1);
                }
            }
        }
        System.out.println(Arrays.toString(args));
    }

}
