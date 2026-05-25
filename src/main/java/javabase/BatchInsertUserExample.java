package javabase;

import java.sql.*;

public class BatchInsertUserExample {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/test?rewriteBatchedStatements=true";
        String user = "root";
        String password = "l15055412310";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            extracted(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void extracted(Statement stmt) throws SQLException {
        int totalBatches = 10000; // 总共执行一万次插入
        int batchSize = 10000; // 每个批次插入1000条记录

        for (int i = 0; i < totalBatches; i++) {
            StringBuilder sql = new StringBuilder("INSERT INTO user (username) VALUES ");

            for (int j = 0; j < batchSize; j++) {
                String username = "User_" + (i * batchSize + j); // 生成唯一的用户名

                sql.append("('").append(username).append("')");

                if (j < batchSize - 1) {
                    sql.append(",");
                } else {
                    sql.append(";");
                }
            }

            stmt.execute(sql.toString()); // 执行插入
            System.out.println("Batch " + (i + 1) + " inserted.");
        }
    }

    private static void select(Statement stmt) throws SQLException {
        int totalBatches = 1; // 总共执行一万次插入
        int batchSize = 1000; // 每个批次插入1000条记录

        stmt.setQueryTimeout(5);
        stmt.setFetchSize(Integer.MIN_VALUE);
        for (int i = 0; i < totalBatches; i++) {
//            StringBuilder sql = new StringBuilder("select * from user;");
            // 执行查询
            try (ResultSet resultSet = stmt.executeQuery("SELECT * FROM user")) {
                // 逐行处理查询结果
                while (resultSet.next()) {
                    // 处理当前行数据
                    String columnData = resultSet.getString("username");
                    System.out.println(columnData);
                    // 可以在这里添加更多的逻辑来处理每行数据
                }
            }
//            ResultSet resultSet = stmt.executeQuery(sql.toString());// 执行插入
            System.out.println("结束");
        }
    }
}

