package com.zhuojl.study.pattern.responsibility;

/**
 * 管家
 */
public class HousekeeperMasterHomeChain extends AbstractMasterHomeChain {

    @Override
    protected void serveMaster(Master master) {
        System.out.println("给主人烧水");
    }

}
