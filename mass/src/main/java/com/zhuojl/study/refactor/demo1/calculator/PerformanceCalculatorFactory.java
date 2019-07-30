package com.zhuojl.study.refactor.demo1.calculator;

import com.zhuojl.study.refactor.demo1.Util;
import com.zhuojl.study.refactor.demo1.pojo.Performance;

public class PerformanceCalculatorFactory {

    public static PerformanceCalculator getCalculator(Performance perf) {
        switch (Util.getPlay(perf).getType()) {
            case "tragedy":
                return new TragedyCalculator(perf);
            case "comedy":
                return new ComedyCalculator(perf);
            default:
                throw new Error("unknown type: ${aPlay.type}");
        }
    }

}
