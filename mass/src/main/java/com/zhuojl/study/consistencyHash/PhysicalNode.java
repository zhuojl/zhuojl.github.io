package com.zhuojl.study.consistencyHash;

import java.util.HashMap;
import java.util.Map;

public class PhysicalNode {

    private String domain;
    private String ip;
    private int port;
    private Map<String, String> data;

    public PhysicalNode(String domain, String ip, int port) {
        this.domain = domain;
        this.ip = ip;
        this.port = port;
        this.data = new HashMap<>();
    }

    public void storeData(String key, String value) {
        data.put(key, value);
    }

    public String getData(String key) {
        return data.get(key);
    }




    @Override
    public String toString() {
        return domain + ":" + ip + ":" + port;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}