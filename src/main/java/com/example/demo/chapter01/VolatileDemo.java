package com.example.demo.chapter01;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 谈谈对volatile的理解：
 * Java虚拟机提供的轻量级同步机制，保证可见性，不保证原子性，禁止指令重排
 * JMM内存模型:
 * 线程解锁前，必须把共享变量的值刷新到主内存
 * 线程加锁前，必须读取主内存的最新值到自己的工作内存
 * 加锁和解锁是同一把锁
 */
class Data {
    private static volatile Data instance = null;
    private volatile int value = 0;

    public void add60() {
        value = 60;
    }

    public int getValue() {
        return value;
    }

    public void addValue() {
        value++;
    }

    private AtomicInteger atomicInteger = new AtomicInteger();

    public void addAtomic() {
        atomicInteger.getAndIncrement();
    }

    public int getAtomic() {
        return atomicInteger.get();
    }

    /**
     * 双检索单例模式下volatile禁止指令重排保证有序性
     * @return
     */
    public Data getInstance() {
        if (null == instance) {
            synchronized (Data.class) {
                if (null == instance) {
                    instance = new Data();
                }
            }
        }
        return instance;
    }
}

public class VolatileDemo {

    public static void main(String[] args) {
//        visibility();
        atomicity();
    }

    /**
     * volatile保证可见性
     */
    public static void visibility() {
        Data data = new Data();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "come in...");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            data.add60();
            System.out.println(Thread.currentThread().getName() + " data update " + data.getValue());
        }, "A").start();
        while (data.getValue() == 0) {
            // 满足可见性后main会知道value的值为60
        }
        System.out.println(Thread.currentThread().getName() + " over...");
    }

    /**
     * volatile不保证原子性
     */
    public static void atomicity() {
        Data data = new Data();
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    data.addValue();
                    data.addAtomic();
                }
            }, String.valueOf(i)).start();
        }
        while (Thread.activeCount() > 2) {
            Thread.yield();
        }
        System.out.println(data.getValue() + " " + data.getAtomic());
    }
}
