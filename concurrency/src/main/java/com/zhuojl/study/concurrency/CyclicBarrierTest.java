package com.zhuojl.study.concurrency;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * TODO
 *
 * @author junliang.zhuo
 * @date 2019-03-19 09:09
 */
public class CyclicBarrierTest {


    public static void main(String[] args) {

        test();

    }

    private static void test(){
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2/*, () -> System.out.println(Thread.currentThread().getName()+" finish")*/);
        new Thread(new BarrierRunner(cyclicBarrier)).start();
        new Thread(new BarrierRunner(cyclicBarrier)).start();
        System.out.println("await two");

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("finish");


    }

    static class BarrierRunner implements Runnable{

        CyclicBarrier cyclicBarrier;

        public BarrierRunner(CyclicBarrier cyclicBarrier) {
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " before await");
                cyclicBarrier.await();
                System.out.println(Thread.currentThread().getName() + " before");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " after");
        }
    }


}
