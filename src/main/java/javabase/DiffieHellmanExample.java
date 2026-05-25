package javabase;

import javax.crypto.KeyAgreement;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class DiffieHellmanExample {

    public static void main(String[] args) throws Exception {
        // 初始化密钥对生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(512); // 512位长的密钥
        // 生成客户端密钥对
        KeyPair clientKeyPair = keyPairGenerator.generateKeyPair();
        // 生成服务器密钥对
        KeyPair serverKeyPair = keyPairGenerator.generateKeyPair();
        // 生成服务器密钥对
        KeyPair attackKeyPair = keyPairGenerator.generateKeyPair();

        // 客户端使用服务器的公钥进行密钥协商
        KeyAgreement clientKeyAgree = KeyAgreement.getInstance("DH");
        clientKeyAgree.init(clientKeyPair.getPrivate());
        clientKeyAgree.doPhase(serverKeyPair.getPublic(), true);
        byte[] clientSharedSecret = clientKeyAgree.generateSecret();

        // 服务器使用客户端的公钥进行密钥协商
        KeyAgreement serverKeyAgree = KeyAgreement.getInstance("DH");
        serverKeyAgree.init(serverKeyPair.getPrivate());
        serverKeyAgree.doPhase(clientKeyPair.getPublic(), true);
        byte[] serverSharedSecret = serverKeyAgree.generateSecret();

        // 攻击者进行密钥协商
        KeyAgreement attackKeyAgree = KeyAgreement.getInstance("DH");
        attackKeyAgree.init(attackKeyPair.getPrivate());
        attackKeyAgree.doPhase(clientKeyPair.getPublic(), true);
        byte[] attackSharedSecret = attackKeyAgree.generateSecret();

        // 检查双方的共享秘密是否相同
        System.out.println("Client Shared Secret: " + Base64.getEncoder().encodeToString(clientSharedSecret));
        System.out.println("Server Shared Secret: " + Base64.getEncoder().encodeToString(serverSharedSecret));
        System.out.println("attack Shared Secret: " + Base64.getEncoder().encodeToString(attackSharedSecret));

        System.out.println("Shared Secrets are equal: " + java.util.Arrays.equals(clientSharedSecret, serverSharedSecret));
    }
}
