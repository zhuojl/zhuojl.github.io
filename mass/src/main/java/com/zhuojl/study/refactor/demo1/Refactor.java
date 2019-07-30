package com.zhuojl.study.refactor.demo1;

import com.zhuojl.study.refactor.demo1.pojo.Invoice;
import com.zhuojl.study.refactor.demo1.pojo.Performance;
import com.zhuojl.study.refactor.demo1.pojo.Play;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 发票打印，戏剧演出团开给客户账单实体类
 * 需求说明见： {@link com.zhuojl.study.refactor.demo1.OriginalDemo}
 * <p>
 * 步骤：
 * 1、抽取方法(计算积分和金额的计算)
 * 2、移除局部变量
 * 3、拆分总金额计算与格式化部分
 * 4、抽取总金额，总积分计算方法
 *
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
        // 客户积分
        String result = "Statement for " + invoice.getCustomer() + "\n";
        for (Performance perf : invoice.getPerformances()) {
            // 字符拼装
            result += " " + getPlay(perf).getName() + ": " + amountFor(perf) + " (" + perf.getAudienceCount() + " seats)\n";
        }

        result += "Amount owed is " + getTotalAmount() + "\n";
        result += "You earned " + getVolumeCredits() + " credits\n";
        System.out.println(result);
    }

    private int getTotalAmount() {
        int result = 0;
        for (Performance perf : invoice.getPerformances()) {
            int thisAmount = amountFor(perf);
            result += thisAmount;
        }
        return result;
    }

    private int getVolumeCredits() {
        int result = 0;
        for (Performance perf : invoice.getPerformances()) {
            // 积分叠加
            result += volumeCreditFor(perf);
        }
        return result;
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

