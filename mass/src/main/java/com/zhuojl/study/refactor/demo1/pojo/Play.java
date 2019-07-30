package com.zhuojl.study.refactor.demo1.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * 戏剧 实体
 *
 * @author zhuojl
 */
@Data
@Builder
public class Play {
    /**
     * 戏剧id
     */
    private String playId;
    /**
     * 戏剧名称
     */
    private String name;
    /**
     * 戏剧类型
     */
    private String type;
}