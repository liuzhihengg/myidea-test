package javabase.understand;

public class Contend {
    static final Object L = new Object();
    static volatile int x = 0;

    static class W extends Thread {
        public void run() {
            long end = System.currentTimeMillis() + 4000;
            while (System.currentTimeMillis() < end) {
                synchronized (L) { x++; }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        for (int i=0;i<8;i++) new W().start();
    }
}
