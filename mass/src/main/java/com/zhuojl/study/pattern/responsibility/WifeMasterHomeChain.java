package com.zhuojl.study.pattern.responsibility;

/**
 * 妻子
 */
public class WifeMasterHomeChain extends AbstractMasterHomeChain {

    @Override
    protected void serveMaster(Master master) {
        System.out.println("给主人。。");
    }


}
