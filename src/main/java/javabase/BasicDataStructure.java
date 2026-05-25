package javabase;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BasicDataStructure {

    public static final int LEN = 10000001;

    public static void main(String[] args) throws IllegalAccessException {
        BasicDataStructure test =new BasicDataStructure();
//        test.testCharset();

        int codePoint = 0x1F601; // 表情符号😁的Unicode代码点
        char high = Character.highSurrogate(codePoint);
        char low = Character.lowSurrogate(codePoint);

        System.out.println("High Surrogate: " + Integer.toHexString(high));
        System.out.println("Low Surrogate: " + Integer.toHexString(low));
    }

    public Object[] testObject() {
        System.out.println("testBoolean");
        Object[] res = new Object[LEN];

        for (int i = 0; i < res.length; i++) {
            res[i] = new Object();
        }

        return res;
    }

    public boolean[] testBoolean() {
        System.out.println("testBoolean");
        boolean[] res = new boolean[LEN];

        for (int i = 0; i < res.length; i++) {
            res[i] = true;
        }

        return res;
    }

    public int[][] testArray() {
        System.out.println("testBoolean");
        int[][] res = new int[LEN][];

        for (int i = 0; i < res.length; i++) {
            res[i] = new int[]{1, 2};
        }

        return res;
    }

    public void testString() throws IllegalAccessException {
        // 获取String类
        Class<?> stringClass = "123".getClass();

        // 获取所有字段，包括静态和非静态
        Field[] fields = stringClass.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true); // 设置字段可访问，即使是私有字段

            // 判断字段是否为静态字段
            boolean isStatic = java.lang.reflect.Modifier.isStatic(field.getModifiers());

            // 获取字段的值
            Object value = field.get(isStatic ? null : "exampleString");

            // 打印字段名、是否静态、字段值
            System.out.println("Field Name: " + field.getName() + ", Static: " + isStatic + ", Value: " + value);
        }
        System.out.println(Charset.defaultCharset());
    }

    public void testCharset() {
        // UTF-8 编码的字节序列
        byte[] utf8Bytes = {(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x83};

        // 将 byte[] 转换为字符串
        String utf8String = new String(utf8Bytes, StandardCharsets.UTF_8);

        System.out.println("UTF-8 字符串: " + utf8String);
    }
}
