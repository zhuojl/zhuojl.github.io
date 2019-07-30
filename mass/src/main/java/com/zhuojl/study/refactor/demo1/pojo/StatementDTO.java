package com.zhuojl.study.refactor.demo1.pojo;

import lombok.Data;

import java.util.List;

@Data
public class StatementDTO {
    private String customer;
    private List<PerformanceResult> performances;
    private Integer totalAmount;
    private Integer totalCredit;
}
