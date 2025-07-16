package cn.ushare.account.util;

import gnu.crypto.hash.HashFactory;
import gnu.crypto.hash.IMessageDigest;
import gnu.crypto.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class HashUtils {
    public static String MD5Encode(String source) {
        byte[] input = source.getBytes();
        IMessageDigest md = HashFactory.getInstance("MD5");
        md.update(input, 0, input.length);
        byte[] digest = md.digest();
        String result = Util.toString(digest);

        return result;
    }

    public static String MD5Encode(File file) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length = -1;

            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            return bytesToString(md.digest());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String MD5Encode(InputStream stream) {
        InputStream fis = stream;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int length = -1;

            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            return bytesToString(md.digest());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException ex) {
               ex.printStackTrace();
            return null;
        } finally {
        }
    }

    public static String bytesToString(byte[] data) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char[] temp = new char[data.length * 2];

        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            temp[i * 2] = hexDigits[b >>> 4 & 0x0f];
            temp[i * 2 + 1] = hexDigits[b & 0x0f];
        }

        return new String(temp);
    }

    public static String RandomEncode() {
        Random random = new Random();
        String alphabeta = "abcdefghijklmnopqrstuvwxyz";

        int charPosition = random.nextInt() % alphabeta.length() - 1;
        if (charPosition < 0) {
            charPosition = 0;
        }

        String uid = alphabeta.charAt(charPosition) + System.currentTimeMillis() + "-" + random.nextLong();

        return HashUtils.MD5Encode(uid);
    }
}
