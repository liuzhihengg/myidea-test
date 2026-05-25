package javabase;

import java.io.*;
import java.util.Base64;

public class SecureClass implements Serializable {
    private static final long serialVersionUID = 1L;
    private String data;

    public SecureClass(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        // 自定义验证逻辑
        if (!isValidData(this.data)) {
            throw new InvalidObjectException("Data validation failed");
        }
    }

    private boolean isValidData(String data) {
        // 简单的验证逻辑：只允许字母和数字
        return data != null && data.matches("[a-zA-Z0-9]+");
    }

    public static void main(String[] args) {
        byte[] serializedObject = receiveSerializedObject();

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedObject))) {
            SecureClass obj = (SecureClass) ois.readObject();
            System.out.println("Received data: " + obj.getData());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static byte[] receiveSerializedObject() {
        // 模拟接收序列化对象
        return Base64.getDecoder().decode(getString());
    }

    private static String getString() {
        MaliciousClass malicious = new MaliciousClass();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(malicious);
            oos.flush();
            String serializedObject = Base64.getEncoder().encodeToString(bos.toByteArray());
            return serializedObject;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

