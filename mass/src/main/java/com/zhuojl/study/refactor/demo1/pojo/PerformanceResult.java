package com.zhuojl.study.refactor.demo1.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * 表演计算结果实体
 *
 * @author zhuojl
 */
@Data
@Builder
public class PerformanceResult {
    /**
     * 表演
     */
    private Performance perf;

    /**
     * 金额
     */
    private Integer amount;

    /**
     * 积分
     */
    private Integer credit;
}
