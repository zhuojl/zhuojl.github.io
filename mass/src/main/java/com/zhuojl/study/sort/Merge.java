package com.zhuojl.study.sort;

/**
 * 归并
 *
 * @author junliang.zhuo
 * @date 2019-05-01 11:13
 */
public class Merge {

    public static void main(String[] args) {
        SortUtil.test(Merge::test);
    }

    private static void test(Integer[] args) {
        merge(args);
    }

    private static Integer[] merge(Integer[] args) {
        if (args.length <= 1) {
            return args;
        }

        Integer[] left = new Integer[args.length / 2];
        Integer[] right = new Integer[args.length - args.length / 2];
        System.arraycopy(args, 0, left, 0, left.length);
        System.arraycopy(args, args.length / 2, right, 0, right.length);

        merge(left);
        merge(right);

        int leftIndex = 0;
        int rightIndex = 0;
        while (leftIndex < left.length || rightIndex < right.length) {
            if (leftIndex < left.length && rightIndex < right.length) {
                if (left[leftIndex] < right[rightIndex]) {
                    args[leftIndex + rightIndex] = left[leftIndex++];
                } else {
                    args[leftIndex + rightIndex] = right[rightIndex++];
                }
            } else if (leftIndex >= left.length) {
                System.arraycopy(right, rightIndex, args, leftIndex + rightIndex, right.length - rightIndex);
                break;
            } else if (rightIndex >= right.length) {
                System.arraycopy(left, leftIndex, args, leftIndex + rightIndex, left.length - leftIndex);
                break;
            }
        }
        return args;
    }

}
