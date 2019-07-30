package com.zhuojl.study.refactor.demo1;

import com.zhuojl.study.refactor.demo1.pojo.Invoice;
import com.zhuojl.study.refactor.demo1.pojo.Performance;

import java.util.Arrays;

public class Client {

    public static void main(String[] args) {
        OriginalDemo originalDemo = new OriginalDemo();
        originalDemo.setInvoice(initInvoice());
        originalDemo.statement();

        Refactor refactor = new Refactor();
        refactor.setInvoice(initInvoice());
        refactor.statement();
    }

    private static Invoice initInvoice() {
        return Invoice.builder()
                .customer("zhuojl")
                .performances(Arrays.asList(
                        Performance.builder()
                                .playId("hamlet")
                                .audienceCount(55)
                                .build(),
                        Performance.builder()
                                .playId("as-like")
                                .audienceCount(35)
                                .build(),
                        Performance.builder()
                                .playId("othello")
                                .audienceCount(40)
                                .build()

                ))
                .build();
    }

}
