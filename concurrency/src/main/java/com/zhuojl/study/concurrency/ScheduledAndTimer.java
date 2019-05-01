package com.zhuojl.study.concurrency;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Timer 和 scheduler 对比
 * 1. timer 支持绝对时间，依赖电脑时钟，而scheduler 支持的是相对时间
 * Timer 是在 运行前 调用java.util.TaskQueue#rescheduleMin(long)，重新入堆,定时时间基于上一次执行的时间+period，所以比较验证的定时，
 * scheduler 是 运行后 调用java.util.concurrent.ScheduledThreadPoolExecutor.ScheduledFutureTask#setNextRunTime() 重新入堆， 入堆时间为当前时间的相对时间
 *
 * 2. 异常处理
 * 如果TimerTask抛出未检查的异常，Timer将会产生无法预料的行为。Timer线程并不捕获异常，所以 TimerTask抛出的未检查的异常会终止timer线程。
 * 此时，已经被安排但尚未执行的TimerTask永远不会再执行了，新的任务也不能被调度。
 *
 * 3. Timer只有一个线程，而scheduler 可以自定义数量
 *
 * @author junliang.zhuo
 * @date 2019-04-30 13:47
 */
public class ScheduledAndTimer {

    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
//        testTimer();
        testScheduler();
    }

    private static void testScheduler() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, r ->
            new Thread(r, "scheduled" + count.getAndIncrement()));
        executorService.schedule(() ->
                System.out.println(Thread.currentThread().getName() + "1:" + System.nanoTime() + executorService)
            , 50, TimeUnit.SECONDS);

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.schedule(() ->
                System.out.println(Thread.currentThread().getName() + "2:" + executorService + System.nanoTime())
            , 20, TimeUnit.SECONDS);
    }

    private static void testTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(timer);
            }
        }, 0, 1000);
    }
}
