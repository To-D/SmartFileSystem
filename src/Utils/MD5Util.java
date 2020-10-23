package Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String byteEncode(byte[] bytes) {
        try {
            // 加密对象，指定加密方式
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            messageDigest.update(bytes);
            // 加密
            byte[] result = messageDigest.digest(bytes);
            return byte2Hex(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String byte2Hex(byte[] data) {
        char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder stringBuilder = new StringBuilder();
        // 处理成十六进制的字符串(通常)
        for (byte bb : data) {
            stringBuilder.append(chars[(bb >> 4) & 15]);
            stringBuilder.append(chars[bb & 15]);
        }
        return stringBuilder.toString();
    }
}


