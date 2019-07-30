package com.zhuojl.study.refactor.demo1.calculator;


import com.zhuojl.study.refactor.demo1.pojo.Performance;


public class PerformanceCalculator {

    public PerformanceCalculator(Performance perf) {
        this.perf = perf;
    }

    protected Performance perf;

    public Integer calculateAmount() {
        throw new Error("subclass responsibility");
    }

    public Integer calculateCredit() {
        return Math.max(this.perf.getAudienceCount() - 30, 0);
    }

}
