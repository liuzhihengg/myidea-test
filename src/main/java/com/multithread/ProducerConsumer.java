package com.multithread;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class ProducerConsumer {

    private static final Deque<Integer> queue = new ArrayDeque<>();

    private static final Object lock = new Object();

    private static final Integer thresold = 32;

    private static final Random random = new Random();

    public void consumer() throws InterruptedException {
        synchronized (lock) {
            while (queue.isEmpty()) {
                System.out.println("队列空了，消费者等待");
                lock.wait();
            }

            System.out.println("消费： " + queue.poll());
            if (queue.size() < thresold) {
                System.out.println("消费者已消费，队列可以继续加入数据了");
                lock.notifyAll(); // 建议使用notifyAll以确保所有等待的线程都能被唤醒
            }
        }
    }

    public void produce() throws InterruptedException {
        synchronized (lock) {
            while (queue.size() >= thresold) {
                lock.wait();
            }

            int i = random.nextInt(100);
            queue.offer(i);
            System.out.println("生产： " + i);
            if (queue.size() == 1) { // 仅当队列从空变为非空时才需要通知
                lock.notifyAll();
            }
        }
    }

    public static void main(String[] args) {
        ProducerConsumer test = new ProducerConsumer();
        new Thread(() ->
        {
            for (int i = 0; i < 1000000; i++) {
                try {
                    test.produce();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(() ->
        {
            for (int i = 0; i < 1000000; i++) {
                try {
                    test.consumer();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
