package cn.ushare.account.util;

/**
 * @Author jixiang.lee
 * @Description
 * @Date create in 17:39 2019/6/3
 * @Modified BY
 */

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA数据签名及数据加密
 */
public class RSAEncryption {
    
    private static final String RSA_KEY_ALGORITHM = "RSA";
    
    private static final String publicKeyBase64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAw5/wpxUY/enBAIVEj1GDzjXUO90FQ9WeE1agCyasX0/WFeK4etUim2q8c/mrHKpoYjbVjniq+eVuw+/mtmt+fgU2JuoSLwvVB4FE0ACND0yj6YeyexG5rVAf7ZghutGpzmDzda0lZMQA0ZHUZ3UQq1SlSSQBbwA9646paw6CJ8DVpE+jEYvg5jnMX+j5iSANhw4BUh4aRIplu/WsTGLARZ+obwuVqWom7Twf5M2WSQ2h6pko9N1iNafEtvpiaf7dfsGXGSrUx4qeWOUAOCE+Zl+H60X7nv3HX42ciusHpPRvXnRwG8R6pGxWLar3sLJdV9ihdd3zkVn92PFmG5SfcwIDAQAB";
    
    private static final String privateKeyBase64 = "";

    // 最大的加密明文长度
    public static final int MAX_ENCRYPT_BLOCK = 245;

    // 最大的解密密文长度
    public static final int MAX_DECRYPT_BLOCK = 256;

    // 分段的加密解密

    private static byte[] segmentDoFinal(InputStream inputStream, Cipher cipher, int maxBlock) throws IOException, IllegalBlockSizeException, BadPaddingException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[maxBlock];
        int length;
        while ((length = inputStream.read(bytes)) != -1) {
            byte[] result = cipher.doFinal(bytes, 0, length);
            byteArrayOutputStream.write(result);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 公钥加密
     */
    public static String encryptByPubKey(String data) throws Exception {
        // 取得公钥
        byte[] pubKey = Base64.decodeBase64(publicKeyBase64);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//        byte[] enSign = cipher.doFinal(data.getBytes());
        byte[] enSign = segmentDoFinal(new ByteArrayInputStream(data.getBytes()), cipher, MAX_ENCRYPT_BLOCK);
        return Base64.encodeBase64String(enSign);
    }
    
    /**
     * 公钥解密
     */
    public static String decryptByPubKey(String data) throws Exception {
        // 取得公钥
        byte[] pubKey = Base64.decodeBase64(publicKeyBase64);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
//        byte[] deSign = cipher.doFinal(Base64.decodeBase64(data));
        byte[] deSign = segmentDoFinal(new ByteArrayInputStream(Base64.decodeBase64(data)), cipher, MAX_DECRYPT_BLOCK);
        return new String(deSign);
    }

    /**
     * 私钥解密
     */
    public static String decryptByPriKey(String data) throws Exception {
        // 取得私钥
        byte[] priKey = Base64.decodeBase64(privateKeyBase64);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
//        byte[] design = cipher.doFinal(Base64.decodeBase64(data));
        byte[] design = segmentDoFinal(new ByteArrayInputStream(Base64.decodeBase64(data)), cipher, MAX_DECRYPT_BLOCK);
        return new String(design);
    }
    
}
