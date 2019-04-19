package com.zhuojl.study.optimize.ifelse;

/**
 * 快速返回
 *
 * @author junliang.zhuo
 * @date 2019-01-31 17:39
 */
public class QuickReturn {

    private boolean b1 = true;
    private boolean b2 = true;
    private boolean b3 = true;

    private void test() {
        if (b1) {
            // doSomething1
            if (b2) {
                // doSomething2
                if (b3) {
                    // doSomething3
                }
            }
        }
        return ;
    }

    private void optimize() {
        if (!b1) {
            return;
        }
        // doSomething1
        if (!b2) {
            return;
        }
        // doSomething2
        if (!b3) {
            return;
        }
        // doSomething3
    }
}
