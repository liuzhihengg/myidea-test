package javabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LogTest {

    private static final Logger logger = LoggerFactory.getLogger(LogTest.class);

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 100;
        int iterations = 10000;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        String infoContent = generateString();
        long startTime = System.currentTimeMillis();



        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    logger.info(infoContent);
//                    logger.info("Logging message {}", j);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken with synchronous logging: " + (endTime - startTime) + " ms");

        executorService = Executors.newFixedThreadPool(threadCount);
        startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    logger.info("Logging message {}", j);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        endTime = System.currentTimeMillis();

        System.out.println("Time taken with asynchronous logging: " + (endTime - startTime) + " ms");
    }


    private static String generateString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            builder.append("Hello World");
        }

        return builder.toString();
    }

}

