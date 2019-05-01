package com.zhuojl.study.sort;

/**
 * 插入排序
 *
 * @author junliang.zhuo
 * @date 2019-05-01 10:49
 */
public class Insert {

    public static void main(String[] args) {
        SortUtil.test(Insert::test);
    }

    public static void test(Integer[] args) {
        for (int i = 1; i < args.length; i++) {
            for (int j = i; j > 0; j--) {
                if (args[j] < args[j - 1]) {
                    SortUtil.swap(args, j, j - 1);
                } else {
                    break;
                }
            }
        }
    }

}
