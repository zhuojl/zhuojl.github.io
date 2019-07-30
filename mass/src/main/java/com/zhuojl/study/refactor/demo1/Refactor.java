package com.zhuojl.study.refactor.demo1;

import com.zhuojl.study.refactor.demo1.pojo.*;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
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
 *
 * @author zjl
 */
@Data
public class Refactor {

    private Invoice invoice;
    /**
     * 演出的戏剧
     */
    private static final Map<String, Play> plays = initPlay();

    /**
     * 打印账单
     */
    public void statement() {
        StatementDTO data = getStatementDTO();
        printData(data);
    }

    private void printData(StatementDTO data) {
        // 客户积分
        String result = "Statement for " + data.getCustomer() + "\n";
        for (PerformanceResult performanceResult : data.getPerformances()) {
            // 字符拼装
            result += " " + getPlay(performanceResult.getPerf()) + ": " + performanceResult.getAmount()
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
                .map(perf -> PerformanceResult.builder()
                        .perf(perf)
                        .amount(amountFor(perf))
                        .credit(volumeCreditFor(perf))
                        .build())
                .collect(Collectors.toList())
        );
        statementDTO.setTotalAmount(statementDTO.getPerformances().stream().mapToInt(PerformanceResult::getAmount).sum());
        statementDTO.setTotalCredit(statementDTO.getPerformances().stream().mapToInt(PerformanceResult::getCredit).sum());
        return statementDTO;
    }

    private int volumeCreditFor(Performance perf) {
        int result = Math.max(perf.getAudienceCount() - 30, 0);
        // 戏剧的额外积分
        if ("comedy" == getPlay(perf).getType()) {
            result += perf.getAudienceCount() / 5;
        }
        return result;
    }

    private Play getPlay(Performance perf) {
        return plays.get(perf.getPlayId());
    }

    private int amountFor(Performance perf) {
        int result;
        switch (getPlay(perf).getType()) {
            case "tragedy":
                result = 40000;
                if (perf.getAudienceCount() > 30) {
                    result += 1000 * (perf.getAudienceCount() - 30);
                }
                break;
            case "comedy":
                result = 30000;
                if (perf.getAudienceCount() > 20) {
                    result += 10000 + 500 * (perf.getAudienceCount() - 20);
                }
                result += 300 * perf.getAudienceCount();
                break;
            default:
                throw new RuntimeException("unknown type: " + getPlay(perf).getType());
        }
        return result;
    }

    private static Map<String, Play> initPlay() {
        Map<String, Play> playMap = new HashMap<>();
        Play play = Play.builder()
                .playId("hamlet")
                .name("Hamlet")
                .type("tragedy")
                .build();
        playMap.put(play.getPlayId(), play);
        play = Play.builder()
                .playId("as-like")
                .name("As You Like It")
                .type("comedy")
                .build();
        playMap.put(play.getPlayId(), play);
        play = Play.builder()
                .playId("othello")
                .name("Othello")
                .type("tragedy")
                .build();
        playMap.put(play.getPlayId(), play);
        return playMap;
    }

}

