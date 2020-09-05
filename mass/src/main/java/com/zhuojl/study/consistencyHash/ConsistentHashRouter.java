package com.zhuojl.study.consistencyHash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * copy from https://github.com/fanfish/ConsistentHash
 *
 * 疑问：
 * TODO
 * hash算法中，有提到动态增减节点的问题，但是看了几篇 一致性hash的实现，都没有处理中途加减节点。
 * @author songwenjun
 */
public class ConsistentHashRouter {

    private SortedMap<Long, VirtualNode> ring = new TreeMap<Long, VirtualNode>();
    private List<PhysicalNode> physicalNodes = new ArrayList<>();
    private MD5Hash hashfunction = new MD5Hash();

    public ConsistentHashRouter(Collection<PhysicalNode> pNodes, int vnodeCount) {
        for (PhysicalNode pNode : pNodes) {
            addNode(pNode, vnodeCount);
        }
    }

    public void storeData(String key, String value) {
        PhysicalNode physicalNode = getNode(key);
        if (Objects.nonNull(physicalNode)) {
            physicalNode.storeData(key, value);
        }
    }

    public List<PhysicalNode> getPhysicalNodes() {
        return physicalNodes;
    }



    public void addNode(PhysicalNode pNode, int vnodeCount) {
        physicalNodes.add(pNode);
        int existingReplicas = getReplicas(pNode.toString());
        for (int i = 0; i < vnodeCount; i++) {
            VirtualNode vNode = new VirtualNode(pNode, i + existingReplicas);
            ring.put(hashfunction.hash(vNode.toString()), vNode);
        }
    }

    public void removeNode(PhysicalNode pNode) {
        physicalNodes.remove(pNode);
        Iterator<Long> it = ring.keySet().iterator();
        while (it.hasNext()) {
            Long key = it.next();
            VirtualNode virtualNode = ring.get(key);
            if (virtualNode.matches(pNode.toString())) {
                it.remove();
            }
        }
    }

    public PhysicalNode getNode(String key) {
        if (ring.isEmpty()) {
            return null;
        }
        Long hashKey = hashfunction.hash(key);
        SortedMap<Long, VirtualNode> tailMap = ring.tailMap(hashKey);
        hashKey = tailMap != null && !tailMap.isEmpty() ? tailMap.firstKey() : ring.firstKey();
        return ring.get(hashKey).getParent();
    }

    public int getReplicas(String nodeName) {
        int replicas = 0;
        for (VirtualNode node : ring.values()) {
            if (node.matches(nodeName)) {
                replicas++;
            }
        }
        return replicas;
    }

    private static class MD5Hash {
        MessageDigest instance;

        public MD5Hash() {
            try {
                instance = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
            }
        }

        long hash(String key) {
            instance.reset();
            instance.update(key.getBytes());
            byte[] digest = instance.digest();

            long h = 0;
            for (int i = 0; i < 4; i++) {
                h <<= 8;
                h |= ((int) digest[i]) & 0xFF;
            }
            return h;
        }
    }

    ;
}