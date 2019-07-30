package com.zhuojl.study.refactor.demo1.pojo;


import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 发票实体
 *
 * @author zhuojl
 */
@Data
@Builder
public class Invoice {
    /**
     * 客户（邀请戏剧团表演的人，非观看者）
     */
    private String customer;
    /**
     * 演出集合
     */
    private List<Performance> performances;
}
