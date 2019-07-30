package com.zhuojl.study.refactor.demo1.calculator;

import com.zhuojl.study.refactor.demo1.pojo.Performance;

public class TragedyCalculator extends PerformanceCalculator {


    public TragedyCalculator(Performance perf) {
        super(perf);
    }

    @Override
    public Integer calculateAmount() {
        int result = 40000;
        if (perf.getAudienceCount() > 30) {
            result += 1000 * (perf.getAudienceCount() - 30);
        }
        return result;
    }

    @Override
    public Integer calculateCredit() {
        return super.calculateCredit();
    }
}
