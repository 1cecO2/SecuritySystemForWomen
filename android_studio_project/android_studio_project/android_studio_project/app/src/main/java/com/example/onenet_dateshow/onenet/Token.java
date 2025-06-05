package com.example.onenet_dateshow.onenet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Token {

    public static String assembleToken(String version, String resourceName, String expirationTime, String signatureMethod, String accessKey)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        StringBuilder sb = new StringBuilder();
        String res = URLEncoder.encode(resourceName, "UTF-8");
        String sig = URLEncoder.encode(generatorSignature(version, resourceName, expirationTime, accessKey, signatureMethod), "UTF-8");

        // 修正版本参数
        sb.append("version=")
                .append(version) // 关键修复点
                .append("&res=")
                .append(res)
                .append("&et=")
                .append(expirationTime)
                .append("&method=")
                .append(signatureMethod)
                .append("&sign=")
                .append(sig);
        return sb.toString();
    }

    public static String generatorSignature(String version, String resourceName, String expirationTime, String accessKey, String signatureMethod)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String encryptText = expirationTime + "\n" + signatureMethod + "\n" + resourceName + "\n" + version;
        byte[] bytes = HmacEncrypt(encryptText, accessKey, signatureMethod);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] HmacEncrypt(String data, String key, String signatureMethod)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signinKey = new SecretKeySpec(Base64.getDecoder().decode(key), "Hmac" + signatureMethod.toUpperCase());
        Mac mac = Mac.getInstance("Hmac" + signatureMethod.toUpperCase());
        mac.init(signinKey);
        return mac.doFinal(data.getBytes());
    }

    public enum SignatureMethod {
        SHA1, SHA256, MD5
    }

    public static String token() throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        String version = "2022-05-01"; // 确保与API要求的版本一致
        String resourceName = "userid/371249";
        String expirationTime = String.valueOf(System.currentTimeMillis() / 1000 + 3600);
        String signatureMethod = SignatureMethod.SHA1.name().toLowerCase();
        String accessKey = "fDhNKs10dhgtf+P/ev1cEFEpgJKkwF3ziSDQ/Y2Pb3wAmH/0c6LkYjBuU17HdkzL5CCkYTaGMFl7WuPtwGLVyQ==";
        return assembleToken(version, resourceName, expirationTime, signatureMethod, accessKey);
    }
}