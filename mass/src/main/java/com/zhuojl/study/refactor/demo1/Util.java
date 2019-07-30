package com.zhuojl.study.refactor.demo1;

import com.zhuojl.study.refactor.demo1.pojo.Performance;
import com.zhuojl.study.refactor.demo1.pojo.Play;

import java.util.HashMap;
import java.util.Map;

public class Util {

    /**
     * 演出的戏剧
     */
    private static final Map<String, Play> plays = initPlay();

    public static Play getPlay(Performance perf) {
        return plays.get(perf.getPlayId());
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
