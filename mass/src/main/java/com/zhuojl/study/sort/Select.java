package com.zhuojl.study.sort;

/**
 * 选择
 *
 * @author junliang.zhuo
 * @date 2019-05-01 10:57
 */
public class Select {

    public static void main(String[] args) {
        SortUtil.test(Select::test);
    }

    public static void test(Integer[] args) {
        int temp;
        int index;
        for (int i = 0; i < args.length; i++) {
            temp = args[i];
            index = i;
            for (int j = i; j < args.length - 1; j++) {
                if (args[j + 1] < temp) {
                    temp = args[j + 1];
                    index = j + 1;
                }
            }
            SortUtil.swap(args, index, i);
        }
    }
}
