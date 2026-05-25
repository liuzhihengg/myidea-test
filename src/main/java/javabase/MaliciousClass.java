package javabase;

import java.io.*;

public class MaliciousClass implements Serializable {
    private static final long serialVersionUID = 1L;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // 恶意代码
        System.out.println("我来攻击了");
    }
}

