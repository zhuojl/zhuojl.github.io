package com.zhuojl.study.refactor.demo1.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * 表演实体
 *
 * @author zhuojl
 */
@Data
@Builder
public class Performance {
    /**
     * 表演节目id
     */
    private String playId;
    /**
     * 观众数量
     */
    private Integer audienceCount;
}
