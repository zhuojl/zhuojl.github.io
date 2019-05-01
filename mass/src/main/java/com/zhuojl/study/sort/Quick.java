package com.zhuojl.study.sort;

/**
 * 快速
 *
 * @author junliang.zhuo
 * @date 2019-05-01 11:13
 */
public class Quick {

    public static void main(String[] args) {
        SortUtil.test(Quick::test);
    }

    private static void test(Integer[] args) {
        sort(args, 0, args.length - 1);
    }

    /**
     * // 找到一个数的位置，把数组拆分为两份，再拆分
     */
    private static void sort(Integer[] args, int _startIndex, int _endIndex) {
        if (_startIndex == _endIndex) {
            return;
        }

        int startIndex = _startIndex;
        int endIndex = _endIndex;
        int tag = args[startIndex];
        int index = startIndex;

        while (startIndex < index || endIndex > startIndex) {
            if (startIndex < index) {
                if (args[startIndex] > tag) {
                    SortUtil.swap(args, index, startIndex);
                    endIndex--;
                    index = startIndex;
                } else {
                    startIndex++;
                }
            }
            if (endIndex > index) {
                if (args[endIndex] < tag) {
                    SortUtil.swap(args, index, endIndex);
                    startIndex++;
                    index = endIndex;
                } else {
                    endIndex--;
                }
            }
        }

        if (index - 1 > _startIndex) {
            sort(args, _startIndex, index - 1);
        }
        if (index + 1 < _endIndex) {
            sort(args, index + 1, _endIndex);
        }

    }

}
