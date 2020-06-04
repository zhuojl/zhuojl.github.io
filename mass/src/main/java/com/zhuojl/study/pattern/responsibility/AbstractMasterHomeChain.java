package com.zhuojl.study.pattern.responsibility;

import java.util.Objects;

/**
 * 责任链处理
 */
public abstract class AbstractMasterHomeChain implements MasterHome {

    protected MasterHome nextChain;

    public void setNextChain(MasterHome nextChain) {
        this.nextChain = nextChain;
    }

    protected boolean isMaster(Master master) {
        return true;
    }

    @Override
    public void masterHome(Master master) {
        if (isMaster(master)) {
            serveMaster(master);
        }

        if (Objects.nonNull(nextChain)) {
            nextChain.masterHome(master);
        }
    }

    protected abstract void serveMaster(Master master);


}
