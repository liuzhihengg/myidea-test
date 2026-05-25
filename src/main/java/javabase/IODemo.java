package javabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class IODemo {

    public static void main(String[] args) {
        IODemo demo = new IODemo();
        demo.testURI();
    }

    public void testFile() {
        File file = new File("/");
        System.out.println(file.getParentFile());
    }

    public void testReader() {
        String filePath = "/Users/tiaojiheng/Desktop/param.txt"; // 指定要读取的文件路径

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // 逐行读取文本并处理每一行
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testURI() {
        try {
            URI uri = new URI("/");

            System.out.println("Scheme: " + uri.getScheme());
            System.out.println("Authority: " + uri.getAuthority());
            System.out.println("Path: " + uri.getPath());
            System.out.println("Query: " + uri.getQuery());
            System.out.println("Fragment: " + uri.getFragment());

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
