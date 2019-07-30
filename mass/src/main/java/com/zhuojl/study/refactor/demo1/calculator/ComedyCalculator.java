package com.zhuojl.study.refactor.demo1.calculator;

import com.zhuojl.study.refactor.demo1.pojo.Performance;

public class ComedyCalculator extends PerformanceCalculator {

    public ComedyCalculator(Performance perf) {
        super(perf);
    }

    @Override
    public Integer calculateAmount() {
        int result = 30000;
        if (perf.getAudienceCount() > 20) {
            result += 10000 + 500 * (perf.getAudienceCount() - 20);
        }
        result += 300 * perf.getAudienceCount();
        return result;
    }

    @Override
    public Integer calculateCredit() {
        int result = super.calculateCredit();
        result += perf.getAudienceCount() / 5;
        return result;
    }
}
