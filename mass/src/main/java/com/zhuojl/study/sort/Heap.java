package com.zhuojl.study.sort;

/**
 * 堆排序
 *
 * @author junliang.zhuo
 * @date 2019-05-01 12:32
 */
public class Heap {

    public static void main(String[] args) {
        SortUtil.test(Heap::test);
    }

    public static void test(Integer[] args) {
        heapFy(args);
        int deleted;
        int child;
        for (int length = args.length; length > 0; ) {
            deleted = args[--length];
            args[length] = args[0];
            // 找左右子节点最小的一个,留着下次做
            int index = 0;
            child = 2 * index + 1;
        }
    }

    private static void heapFy(Integer[] args) {
        int index;
        int temp;
        for (int i = 1; i < args.length; i++) {
            index = i;
            while (index > 0) {
                temp = args[(index + 1) / 2 - 1];
                if (temp > args[index]) {
                    SortUtil.swap(args, index, (index + 1) / 2 - 1);
                    index = (index + 1) / 2 - 1;
                } else {
                    break;
                }
            }
        }
    }

}
