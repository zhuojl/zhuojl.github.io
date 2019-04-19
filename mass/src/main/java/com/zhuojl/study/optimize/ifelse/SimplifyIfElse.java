package com.zhuojl.study.optimize.ifelse;

/**
 * idea自动优化 if else
 *
 * @author junliang.zhuo
 * @date 2019-01-31 17:00
 */
public class SimplifyIfElse {

    private static int i0 = 1;
    private static int i1 = 1;
    private static int i2 = 1;
    private static int i3 = 1;

    private static boolean test() {
        if (i0 > i1) {
            return true;
        }
        if (i1 > i2) {
            return true;
        }
        if (i2 > i3) {
            return true;
        }
        return false;
    }

    private static boolean simplify() {
        return i0 > i1 || i1 > i2 || i2 > i3;
    }


    private static boolean test1() {
        if (i0 > i1) {
            return true;
        }
        if (i1 > i2) {
            return false;
        }
        if (i2 > i3) {
            return true;
        }
        return false;
    }

    private static boolean simplify1() {
        return i0 > i1 || i1 <= i2 && i2 > i3;
    }
}
