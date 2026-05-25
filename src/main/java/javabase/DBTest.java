package javabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DBTest {
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    private static final String USER = "root";
    private static final String PASSWORD = "l15055412310";

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(100);

        // 记录开始时间
        long startTime = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            executor.submit(new DBOperation());
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        } finally {
            // 记录结束时间
            long endTime = System.nanoTime();
            // 计算并打印耗时
            long duration = (endTime - startTime);
            System.out.println("Total execution time: " + duration/1_000_000_000 + " seconds");
        }
    }

    static class DBOperation implements Runnable {
        @Override
        public void run() {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                Random random = new Random();
                for (int i = 0; i < 1000; i++) {
                    int operation = random.nextInt(100);
                    if (operation < 50) {
                        // Perform insert
                        try (PreparedStatement stmt = connection.prepareStatement(
                                "INSERT INTO user (username, created_at, updated_at) VALUES (?, NOW(), NOW())")) {
                            stmt.setString(1, "User" + random.nextInt(Integer.MAX_VALUE));
                            stmt.executeUpdate();
                        }
                    } else if (operation < 75) {
                        // Perform delete
                        try (PreparedStatement stmt = connection.prepareStatement(
                                "DELETE FROM user WHERE id = ?")) {
                            stmt.setInt(1, random.nextInt(100000000)); // Assuming id range
                            stmt.executeUpdate();
                        }
                    } else {
                        // Perform update
                        try (PreparedStatement stmt = connection.prepareStatement(
                                "UPDATE user SET username = ? WHERE id = ?")) {
                            stmt.setString(1, "UpdatedUser" + random.nextInt(Integer.MAX_VALUE));
                            stmt.setInt(2, random.nextInt(100000000)); // Assuming id range
                            stmt.executeUpdate();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

