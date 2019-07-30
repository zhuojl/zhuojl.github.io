package com.zhuojl.study.refactor.demo1;

import com.zhuojl.study.refactor.demo1.pojo.Invoice;
import com.zhuojl.study.refactor.demo1.pojo.Performance;
import com.zhuojl.study.refactor.demo1.pojo.Play;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 发票打印，戏剧演出团开给客户账单实体类
 * <p>
 * 重构第一章示例
 * 需求：
 * 设想有一个戏剧演出团，演员们经常要去各种场合表演戏剧。通常客户(customer) 会指定几出剧目，
 * 而剧团则根据观众(audience)人数及剧目类型来向客户收费。该团目前出演两种戏剧:悲剧(tragedy)和喜剧(comedy)。
 * 给客户发出账单时， 剧团还会根据到场观众的数量给出“观众量积分”(volume credit)优惠，下次客户 再请剧团表演时可以使用积分获得折扣—
 * 你可以把它看作一种提升客户忠诚度的 方式。
 * @author zjl
 */
@Data
public class OriginalDemo {

    private Invoice invoice;
    /**
     * 演出的戏剧
     */
    private static final Map<String, Play> plays = initPlay();

    /**
     * 打印账单
     *
     */
    public void statement() {
        // 总金额
        int totalAmount = 0;
        // 客户积分
        int volumeCredits = 0;
        String result = "Statement for " + invoice.getCustomer() + "\n";
        for (Performance perf : invoice.getPerformances()) {
            Play play = plays.get(perf.getPlayId());
            int thisAmount;
            switch (play.getType()) {
                case "tragedy":
                    thisAmount = 40000;
                    if (perf.getAudienceCount() > 30) {
                        thisAmount += 1000 * (perf.getAudienceCount() - 30);
                    }
                    break;
                case "comedy":
                    thisAmount = 30000;
                    if (perf.getAudienceCount() > 20) {
                        thisAmount += 10000 + 500 * (perf.getAudienceCount() - 20);
                    }
                    thisAmount += 300 * perf.getAudienceCount();
                    break;
                default:
                    throw new RuntimeException("unknown type: " + play.getType());
            }
            // 积分叠加
            volumeCredits += Math.max(perf.getAudienceCount() - 30, 0);
            // 戏剧的额外积分
            if ("comedy" == play.getType()) {
                volumeCredits += perf.getAudienceCount() / 5;
            }
            // 结果拼装
            result += " " + play.getName() + ": " + thisAmount + " (" + perf.getAudienceCount() + " seats)\n";
            totalAmount += thisAmount;
        }
        result += "Amount owed is " + totalAmount + "\n";
        result += "You earned " + volumeCredits + " credits\n";
        System.out.println(result);
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

