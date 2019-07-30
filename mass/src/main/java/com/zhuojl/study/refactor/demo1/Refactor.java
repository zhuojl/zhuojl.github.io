package com.zhuojl.study.refactor.demo1;

import com.zhuojl.study.refactor.demo1.calculator.PerformanceCalculator;
import com.zhuojl.study.refactor.demo1.calculator.PerformanceCalculatorFactory;
import com.zhuojl.study.refactor.demo1.pojo.*;
import lombok.Data;

import java.util.stream.Collectors;

/**
 * 发票打印，戏剧演出团开给客户账单实体类
 * 需求说明见： {@link com.zhuojl.study.refactor.demo1.OriginalDemo}
 * <p>
 * 步骤：
 * 1、抽取方法(计算积分和金额的计算)
 * 2、移除局部变量
 * 3、拆分总金额计算与格式化部分
 * 4、抽取总金额，总积分计算方法
 * 5、抽离计算与展示
 * 6、利用多态取代条件表达式
 *
 * @author zjl
 */
@Data
public class Refactor {

    private Invoice invoice;

    /**
     * 打印账单
     */
    public void statement() {
        StatementDTO data = getStatementDTO();
        printData(data);
        // 或者html展示或者导出，这就不实现了
    }

    private void printData(StatementDTO data) {
        // 客户积分
        String result = "Statement for " + data.getCustomer() + "\n";
        for (PerformanceResult performanceResult : data.getPerformances()) {
            // 字符拼装
            result += " " + performanceResult.getPlay().getName() + ": " + performanceResult.getAmount()
                    + " (" + performanceResult.getPerf().getAudienceCount() + " seats)\n";
        }

        result += "Amount owed is " + data.getTotalAmount() + "\n";
        result += "You earned " + data.getTotalCredit() + " credits\n";
        System.out.println(result);
    }

    private StatementDTO getStatementDTO() {
        StatementDTO statementDTO = new StatementDTO();
        statementDTO.setCustomer(invoice.getCustomer());
        statementDTO.setPerformances(invoice.getPerformances().stream()
                .map(perf -> performanceCalculate(perf))
                .collect(Collectors.toList())
        );
        statementDTO.setTotalAmount(statementDTO.getPerformances().stream().mapToInt(PerformanceResult::getAmount).sum());
        statementDTO.setTotalCredit(statementDTO.getPerformances().stream().mapToInt(PerformanceResult::getCredit).sum());
        return statementDTO;
    }

    private PerformanceResult performanceCalculate(Performance perf) {
        PerformanceCalculator calculator = PerformanceCalculatorFactory.getCalculator(perf);
        return PerformanceResult.builder()
                .perf(perf)
                .play(Util.getPlay(perf))
                .amount(calculator.calculateAmount())
                .credit(calculator.calculateCredit())
                .build();
    }

}

