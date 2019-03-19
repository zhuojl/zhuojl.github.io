package com.zhuojl.study.concurrency;

import java.util.concurrent.Semaphore;

/**
 * TODO
 *
 * @author junliang.zhuo
 * @date 2019-03-19 09:08
 */
public class SemaphoreTest<E> {


    private final Semaphore availableItems, availableSpace;
    private final E[] items;
    private int putPosition = 0, takePosition = 0;

    public SemaphoreTest(int capacity) {
        availableItems = new Semaphore(0);
        availableSpace = new Semaphore(capacity);
        items = (E[]) new Object[capacity];
    }

    public boolean isEmpty() {
        return availableItems.availablePermits() == 0;
    }

    public boolean isFull() {
        return availableSpace.availablePermits() == 0;
    }

    public void put(E x) throws InterruptedException {
        availableSpace.acquire();
        doInert(x);
        availableSpace.release();
    }

    public E take() throws InterruptedException {
        availableItems.acquire();
        E e = doExact();
        availableItems.release();
        return e;
    }

    private synchronized void doInert(E x) {
        int i = putPosition;
        items[i] = x;
        putPosition = (++i == items.length) ? 0 : i;
    }

    private synchronized E doExact() {
        int i = takePosition;
        E x = items[i];
        items[i] = null;
        takePosition = (++i == items.length) ? 0 : i;
        return x;
    }


}
