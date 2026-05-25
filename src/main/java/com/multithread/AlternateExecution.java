package com.multithread;

import java.util.ArrayDeque;
import java.util.Deque;

public class AlternateExecution {

    static class NumebrPrint implements Runnable {

        private Object lock;

        private Deque<Integer> queue;

        public NumebrPrint(Object lock, Deque<Integer> queue) {
            this.lock = lock;
            this.queue = queue;
        }

        @Override
        public void run() {
            synchronized (lock) {
                for (int i = 0; i < 10; i++) {
                    while (queue.isEmpty()) {
                        System.out.println("不符合打印数字的条件");
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println(i);
                    queue.poll();
                    lock.notify();
                }
            }
        }
    }

    static class AlaphPrint implements Runnable {

        private Object lock;

        private Deque<Integer> queue;

        public AlaphPrint(Object lock, Deque<Integer> queue) {
            this.lock = lock;
            this.queue = queue;
        }

        @Override
        public void run() {
            synchronized (lock) {
                for (int i = 0; i < 10; i++) {
                    char c = 'H';
                    while (!queue.isEmpty()) {
                        System.out.println("不符合打印数字的条件");
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    queue.offer(1);
                    System.out.println((char) (c + i));
                    lock.notify();
                }
            }
        }
    }

    public static void main(String[] args) {
        Object lock = new Object();
        Deque<Integer> queue = new ArrayDeque<>();
        queue.offer(1);
        new Thread(new NumebrPrint(lock, queue)).start();
        new Thread(new AlaphPrint(lock, queue)).start();
    }
}
