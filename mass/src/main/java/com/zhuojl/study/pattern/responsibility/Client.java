package com.zhuojl.study.pattern.responsibility;

public class Client {

    public static void main(String[] args) {
        AbstractMasterHomeChain housekeeper = new HousekeeperMasterHomeChain();
        AbstractMasterHomeChain maid = new MaidMasterHomeChain();
        AbstractMasterHomeChain wife = new WifeMasterHomeChain();

        housekeeper.setNextChain(maid);
        maid.setNextChain(wife);

        Master master = new Master();

        housekeeper.masterHome(master);

    }
}
