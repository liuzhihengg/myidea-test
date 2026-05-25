package com.multithread;

// Java代码示例
class ReorderingExample {
    static int x = 0, y = 0;
    static int a = 0, b = 0;

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100000; i++) {
            Thread one = new Thread(() -> {
                a = 1; // Step 1
                x = b; // Step 2

            });

            Thread two = new Thread(() -> {
                b = 1; // Step 3
                y = a; // Step 4

            });

            one.start();
            two.start();
            one.join();
            two.join();

            if (x == 0 && y == 0) {
                System.out.println("(x,y): (" + x + "," + y + ")");
            }
            x = 0;
            y = 0;
            a = 0;
            b = 0;
        }

    }
}

