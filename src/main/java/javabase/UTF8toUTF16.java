package javabase;

public class UTF8toUTF16 {
    public static String decode(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < bytes.length) {
            int b = bytes[i] & 0xFF; // 将byte转为无符号整数
            if (b < 0x80) { // 1字节，ASCII字符
                sb.append((char) b);
                i++;
            } else if (b < 0xE0) { // 2字节
                int b2 = bytes[i + 1] & 0xFF;
                sb.append((char) (((b & 0x1F) << 6) | (b2 & 0x3F)));
                i += 2;
            } else if (b < 0xF0) { // 3字节
                int b2 = bytes[i + 1] & 0xFF;
                int b3 = bytes[i + 2] & 0xFF;
                sb.append((char) (((b & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F)));
                i += 3;
            } else {
                // 对于4字节及以上的序列，在此简化版本中不做处理
                // 实际应用中应该处理这种情况
                throw new IllegalArgumentException("Unsupported UTF-8 sequence");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        byte[] utf8Bytes = new byte[]{(byte) 0xE4, (byte) 0xB8, (byte) 0xAD}; // "中"的UTF-8编码
        String result = decode(utf8Bytes);
        System.out.println(result); // 输出转换后的字符串
    }
}
