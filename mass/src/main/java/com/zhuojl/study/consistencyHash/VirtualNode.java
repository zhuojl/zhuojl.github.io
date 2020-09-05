package com.zhuojl.study.consistencyHash;

public class VirtualNode {
    private int replicaNumber;
    private PhysicalNode parent;

    public VirtualNode(PhysicalNode parent, int replicaNumber) {
        this.replicaNumber = replicaNumber;
        this.parent = parent;
    }

    public boolean matches(String host) {
        return parent.toString().equalsIgnoreCase(host);
    }

    public void storeData(String key, String value) {
        parent.storeData(key, value);
    }

    public String getData(String key) {
        return parent.getData(key);
    }


    @Override
    public String toString() {
        return parent.toString().toLowerCase() + ":" + replicaNumber;
    }

    public int getReplicaNumber() {
        return replicaNumber;
    }

    public void setReplicaNumber(int replicaNumber) {
        this.replicaNumber = replicaNumber;
    }

    public PhysicalNode getParent() {
        return parent;
    }
}