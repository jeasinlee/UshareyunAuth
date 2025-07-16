package cn.ushare.account.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * AES加密工具
 */
public final class AESUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(AESUtil.class);

    //非对称加密秘钥算法
    private static final String KEY_ALGORITHM = "RSA";
    //公钥
    private static final String PUBLIC_KEY = "RSAPublicKey";
    //私钥
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    //RSA秘钥长度  默认1024
    private static final int KEY_SIZE = 1024;

    private static byte[] privateKey;//字节 私钥

    private static byte[] publicKey;//字节 公钥

    private final static String PRIVATE_KEY_STRING = "30820277020100300d06092a864886f70d0101010500048202613082025d02010002818100ca2406d050fdb62d37e553b5dce2c8c27204640e1f3b287f5219efa539a917c56d7681cee6b7664eb45ef8754d425746bd8389a118869b06ffd2bca20906c508d59eb670886db00c4b9a2288eb92d8d580866818943f45f297674e1bc3d9ac269b9988239dfb5b7b93bb71fa8e123160521bb9eae75a04ddfaab9a4bcf57698b02030100010281810092a71e96661c1bdea48de974a90393b996b4dece6c14246672ef4431302b2086e8cab094a6486a9e313831b410d04fb78fe8eda205c42a97226506dbdc06a04d64c3a0d6d954978409cb282edc069f527d464ca2dc477e180303b6d5a18dcc731a6a34ce6d1b528c79ff50b2925a99dba6677cede895df18db9630f1d598fb79024100f28f2b46f7b603b96fa78646daec4545078976564d4ea14a82a666e3b11ce40f466323f491d5264b4d9712bf33c04fe91808050db1afe79a95a9413e9344f0e7024100d5577fdd25af0429a18f9b591a4924b9c82de1a08bbf5c2ee907c528359bb4ef545289d90d665454e0bb9920d0676d4bc941bffaf0280797f18a5ebcba5d19bd02405e9f5744a21191365fc63c6d9bad9c0027c68a31748afe04b11ef4f851f971463c3124af9dcc46da0d74a7cd8b04b4a7bbc635227874a4a6fb4741ba857cd9750240661bcdf8039c43aac547dd7b8508330ab453b964c4de1ef9d8d0be3d638315d362916aa6b7321df4cde71c00479ee901d7de9f4b347c843de5fe6b1f7b372a25024100be082ecb15158d207057d895707efde1186437396b74593ff13d4825388b5baf4a15e8d1fcb6721eaa05bb4c245578b519552f6793adb97c95370977e912e0ee";

    private final static String PUBLIC_KEY_STRING = "30819f300d06092a864886f70d010101050003818d0030818902818100ca2406d050fdb62d37e553b5dce2c8c27204640e1f3b287f5219efa539a917c56d7681cee6b7664eb45ef8754d425746bd8389a118869b06ffd2bca20906c508d59eb670886db00c4b9a2288eb92d8d580866818943f45f297674e1bc3d9ac269b9988239dfb5b7b93bb71fa8e123160521bb9eae75a04ddfaab9a4bcf57698b0203010001";
    //私钥解密

    /**
      * 签名算法
     */
     public static final String SIGN_ALGORITHMS = "SHA1PRNG";
    /**
     * @param data 待解密的数据
     * @param key  私钥
     * @return byte[] 解密数据
     * @throws Exception
     */
    private static byte[] decryptByPrivateKey(byte[] data, byte[] key) throws Exception {
        //取的私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //生产私钥对象
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        //对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        //执行
        return cipher.doFinal(data);
    }

    /**
     * @param data 待加密数据
     * @param key  公钥
     * @return byte[] 加密数据
     * @throws Exception
     */
    private static byte[] encryptByPublicKey(byte[] data, byte[] key) throws Exception {
        //取的公钥
        X509EncodedKeySpec X509KeySpec = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //生产公钥对象
        PublicKey publicKey = keyFactory.generatePublic(X509KeySpec);
        //对数据进行加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        //执行
        return cipher.doFinal(data);
    }

    /**
     * 取的私钥
     *
     * @param keyMap 秘钥map
     * @return byte[] 私钥
     */
    private static byte[] getPrivateKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return key.getEncoded();
    }

    /**
     * 取的公钥
     *
     * @param keyMap 秘钥map
     * @return byte[] 公钥
     */
    private static byte[] getPublicKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return key.getEncoded();
    }

    private static Map<String, Object> initKey() throws Exception {
        //实例化秘钥生产器
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化秘钥生产器
        keyPairGen.initialize(KEY_SIZE);
        //生产秘钥对
        KeyPair keyPair = keyPairGen.generateKeyPair();
        //公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        //秘钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        //封装秘钥
        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }


    /**
     * 将16进制字符串还原为字节数组.
     */
    private static final byte[] hexStrToBytes(String s) {
        byte[] bytes;

        bytes = new byte[s.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }

        return bytes;
    }


    /**
     * 本方法使用SHA1withRSA签名算法产生签名
     *
     * @param priKey 签名时使用的私钥(16进制编码)
     * @param src    签名的原字符串
     * @return String 签名的返回结果(16进制编码)。当产生签名出错的时候，返回null。
     */
    private static String generateSHA1withRSASigature(String priKey, String src) {
        try {

            Signature sigEng = Signature.getInstance("SHA1withRSA");

            byte[] pribyte = hexStrToBytes(priKey.trim());

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pribyte);
            KeyFactory fac = KeyFactory.getInstance("RSA");

            RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
            sigEng.initSign(privateKey);
            sigEng.update(src.getBytes());

            byte[] signature = sigEng.sign();
            return new String(Hex.encodeHex(signature));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 本方法使用SHA1withRSA签名算法验证签名
     *
     * @param pubKey 验证签名时使用的公钥(16进制编码)
     * @param sign   签名结果(16进制编码)
     * @param src    签名的原字符串
     * @return String 签名的返回结果(16进制编码)
     */
    private static boolean verifySHA1withRSASigature(String pubKey, String sign, String src) {
        try {
            Signature sigEng = Signature.getInstance("SHA1withRSA");

            byte[] pubbyte = hexStrToBytes(pubKey.trim());

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubbyte);
            KeyFactory fac = KeyFactory.getInstance("RSA");
            RSAPublicKey rsaPubKey = (RSAPublicKey) fac.generatePublic(keySpec);

            sigEng.initVerify(rsaPubKey);
            sigEng.update(src.getBytes());

            byte[] sign1 = hexStrToBytes(sign);
            return sigEng.verify(sign1);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证签名
     *
     * @param sign 签名值
     * @param data 加密数据
     * @throws Exception
     */
    private static void verifySign(String sign, String data) throws Exception {
        //验证签名
        Boolean falg = verifySHA1withRSASigature(PUBLIC_KEY_STRING, sign, data);
        System.out.println("签名状态：\t" + falg);

        if (falg) {
            byte[] endcryptData = hexStrToBytes(data);
            //私钥解密
            byte[] decryptData = AESUtil.decryptByPrivateKey(endcryptData, privateKey);
            String decryptStr = new String(decryptData);
            System.out.println("解密后：\t" + decryptStr);
        }

    }

    /**
     * 加密
     *
     * @param plaintext 明文
     * @return
     */
    public static String encrypt(String plaintext) {
        if (TextUtils.isEmpty(plaintext)) {
            return null;
        }
        LOGGER.debug("加密前原文:\t" + plaintext);
        publicKey = hexStrToBytes(PUBLIC_KEY_STRING);
        try {
            byte[] endcryptData = AESUtil.encryptByPublicKey(plaintext.getBytes(), publicKey);
            String mw = Base64.encodeBase64String(endcryptData);
            LOGGER.debug("加密后密文:\t" + mw);
            return mw;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 解密
     *
     * @param ciphertext 密文
     * @return
     */
    public static String decryption(String ciphertext) {
        if (TextUtils.isEmpty(ciphertext)) {
            return null;
        }
        LOGGER.debug("加密前密文:\t" + ciphertext);
        privateKey = hexStrToBytes(PRIVATE_KEY_STRING);
        try {
            byte[] decryptData = AESUtil.decryptByPrivateKey(Base64.decodeBase64(ciphertext), privateKey);
            String mw = new String(decryptData);
            LOGGER.debug("解密后明文:\t" + mw);
            return mw;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /*
     * 加密
     * 1.构造密钥生成器
     * 2.根据ecnodeRules规则初始化密钥生成器
     * 3.产生密钥
     * 4.创建和初始化密码器
     * 5.内容加密
     * 6.返回字符串
     */
    public static String AESEncode(String encodeRules, String content) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(SIGN_ALGORITHMS);
            secureRandom.setSeed(encodeRules.getBytes());

            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return parseByte2HexStr(result); // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }catch (InvalidKeyException e) {
            e.printStackTrace();
        }catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * 解密
     * 解密过程：
     * 1.同加密1-4步
     * 2.将加密后的字符串反纺成byte[]数组
     * 3.将加密内容解密
     */
    public static String AESDncode(String encodeRules, String content) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(SIGN_ALGORITHMS);
            secureRandom.setSeed(encodeRules.getBytes());

            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(parseHexStr2Byte(content));
            return new String(result, "utf-8"); // 解密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }catch (InvalidKeyException e) {
            e.printStackTrace();
        }catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 将二进制转换成16进制
     * @method parseByte2HexStr
     * @param buf
     * @return
     * @throws
     * @date v1.0
     */
    public static String parseByte2HexStr(byte buf[]){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < buf.length; i++){
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     * @method parseHexStr2Byte
     * @param hexStr
     * @return
     * @throws
     * @date v1.0
     */
    public static byte[] parseHexStr2Byte(String hexStr){
        if(hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
		    /*Map<String,Object> keyMap = AESUtil.initKey();
        byte[] privateKey = AESUtil.getPrivateKey(keyMap);
        byte[] publicKey = AESUtil.getPublicKey(keyMap);

        //Hex.encodeHex 做16进制编码处理
        System.out.println("16进制 公钥字符串：\t"+new String(Hex.encodeHex(publicKey)));
        System.out.println("16进制 私钥字符串：\t"+new String(Hex.encodeHex(privateKey)));*/

//		privateKey = hexStrToBytes(PRIVATE_KEY_STRING);
//
//		publicKey = hexStrToBytes(PUBLIC_KEY_STRING);

//        String str = "5#1#1#1491462150844#7776EWQEWERFSD";
//
//        System.out.println("加密前的明文:" + str);
//        String miwen = encrypt(str);
//        System.out.println("密文:" + miwen);
//        System.out.println("解密后的明文:" + decryption(miwen));
//
//
//        byte[] inputStr = str.getBytes();
//        System.out.println("原文：\t" + str);

		/*//进行数据签名
        String sign = generateSHA1withRSASigature(privateKeyString,str);
        System.out.println("签名值：\t"+sign);

        Boolean falg = verifySHA1withRSASigature(publicKeyString,sign,str);
        System.out.println("签名状态：\t"+falg);
        if(falg){
        */

//            System.out.println("公钥：\t"+Base64.encodeBase64String(publicKey));
//            System.out.println("私钥：\t"+Base64.encodeBase64String(privateKey));

        //公钥加密
//        long start1 = System.currentTimeMillis();
//        byte[] endcryptData = AESUtil.encryptByPublicKey(inputStr, publicKey);
//        System.out.println("加密后：\t" + Base64.encodeBase64String(endcryptData));
//        System.out.println("加密耗时:" + (System.currentTimeMillis() - start1) + " ms");
//
//        //私钥解密
//        long start2 = System.currentTimeMillis();
//        byte[] decryptData = AESUtil.decryptByPrivateKey(endcryptData, privateKey);
//        String decryptStr = new String(decryptData);
//        System.out.println("解密后：\t" + decryptStr);
//        System.out.println("解密耗时:" + (System.currentTimeMillis() - start2) + " ms");
//        }

//        System.out.println("签名验证不通过...end");
//
//		//以下是先通过私钥对原始数据进行加密，再进行签名
//		//公钥加密
//		byte[] endcryptData = AESTest.encryptByPublicKey(inputStr, publicKey);
//		System.out.println("加密后：\t"+Base64.encodeBase64String(endcryptData));
//
//		//进行数据签名
//		String endcryptDataStr = new String(Hex.encodeHex(endcryptData));
//		String sign = generateSHA1withRSASigature(privateKeyString,endcryptDataStr);
//		System.out.println("签名值：\t"+sign);
//
//		verifySign(sign,endcryptDataStr);

    }


}
