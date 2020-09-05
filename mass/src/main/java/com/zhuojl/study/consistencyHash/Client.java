package com.zhuojl.study.consistencyHash;


import java.util.ArrayList;
import java.util.List;

public class Client {

    public static void main(String[] args) {
        System.out.println(Math.round(-1.6));
        List<PhysicalNode> physicalNodes = new ArrayList<>(3);
        physicalNodes.add(new PhysicalNode("baidu", "1.1.1.1", 8080));
        physicalNodes.add(new PhysicalNode("baidu", "1.1.1.1", 8081));
        physicalNodes.add(new PhysicalNode("baidu", "1.1.1.1", 8082));
        ConsistentHashRouter hashRouter = new ConsistentHashRouter(physicalNodes, 2);

        hashRouter.storeData("test", "name");

        hashRouter.getPhysicalNodes().forEach(node -> {
            System.out.println("node: " + node);
            System.out.println("data: " + node.getData());
        });

    }
}
