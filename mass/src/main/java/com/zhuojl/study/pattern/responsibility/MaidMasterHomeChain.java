package com.zhuojl.study.pattern.responsibility;

/**
 * 女仆
 */
public class MaidMasterHomeChain extends AbstractMasterHomeChain {

    @Override
    protected void serveMaster(Master master) {
        System.out.println("给主人搓背");
    }

}
