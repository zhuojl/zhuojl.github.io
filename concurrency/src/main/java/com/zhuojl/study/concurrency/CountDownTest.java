package com.zhuojl.study.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author:junliang.zhuo
 * @date :2018/12/20
 */
public class CountDownTest {

    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(5);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

        for (int i = 0; i < 5; i++) {
            int time = i;
            executor.execute(() -> {
                try {
                    Thread.sleep(300L * time);
                    System.out.println(Thread.currentThread().getName() + ":" + time + " countDown");
                    countDownLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            int time = i;
            executor.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + ":" + time + " start wait");
                    countDownLatch.await();
                    System.out.println(Thread.currentThread().getName() + ":" + time + " start wait come");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
    }

}
