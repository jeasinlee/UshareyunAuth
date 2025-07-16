package cn.ushare.account.admin.portal.service;

import cn.ushare.account.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class PortalUtil {

	/**
	 * UDP传输16进制字节流包很容易丢失 出现乱码的几率相当大 所以这里我们先转成16进制字符串再做一个拼接操作
	 *
	 * 注意这里b[ i ] & 0xFF将一个byte和 0xFF进行了与运算。 b[ i ] & 0xFF运算后得出的仍然是个int,那么为何要和
	 * 0xFF进行与运算呢?直接 Integer.toHexString(b[ i ]); 将byte强转为int不行吗?答案是不行的. 其原因在于:
	 * 1.byte的大小为8bits而int的大小为32bits 2.java的二进制采用的是补码形式
	 * byte是一个字节保存的,有8个位,即8个0、1。 8位的第一个位是符号位, 也就是说0000 0001代表的是数字1 1000
	 * 0000代表的就是-1 所以正数最大位0111 1111,也就是数字127 负数最大为1111 1111,也就是数字-128
	 *
	 * @author LeeSon QQ:25901875
	 *
	 */
	public static String Getbyte2HexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex);
		}
		return ("[" + sb.toString() + "]");
	}

	public static String Getbyte2MacString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex);
			if((i<b.length-1)){
				sb.append(":");
			}
		}
		return sb.toString();
	}

	public static String MacFormat(String ikmac){
		if(StringUtil.isNotBlank(ikmac)){
			ikmac = ikmac.replace("-", "");
			ikmac = ikmac.replace(":", "");
			ikmac = ikmac.trim().toLowerCase();
			StringBuilder sb = new StringBuilder();
			sb.append("");
			int count=ikmac.length();
			for (int i = 1; i <= count; i++) {
				sb.append(ikmac.substring(i-1, i));
				if((i<count)&&(i%2==0)){
					sb.append(":");
				}
			}
			return sb.toString();
		}else {
			return "";
		}
	}

	/**
	 * 去掉分隔符，大写
	 */
	public static String RawMacFormat(String ikmac){
        if(StringUtil.isNotBlank(ikmac)){
            ikmac = ikmac.replace("-", "");
            ikmac = ikmac.replace(":", "");
            ikmac = ikmac.trim().toUpperCase();
            return ikmac;
        }else {
            return "";
        }
    }

	public static String MacFormat1(String ikmac){
		if(StringUtil.isNotBlank(ikmac)){
			ikmac = ikmac.replace("-", "");
			ikmac = ikmac.replace(":", "");
			ikmac = ikmac.trim().toLowerCase();
			return ikmac;
		}else {
			return "";
		}
	}

	public static byte[] SerialNo() {
		byte[] SerialNo = new byte[2];
		short SerialNo_int = (short) (1 + Math.random() * 32767);
		for (int i = 0; i < 2; i++) {
			int offset = (SerialNo.length - 1 - i) * 8;
			SerialNo[i] = (byte) ((SerialNo_int >>> offset) & 0xff);
		}
		return SerialNo;
	}

	public static byte[] SerialNo(short SerialNo_int) {
		byte[] SerialNo = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (SerialNo.length - 1 - i) * 8;
			SerialNo[i] = (byte) ((SerialNo_int >>> offset) & 0xff);
		}
		return SerialNo;
	}

	/**
     * 生成Authenticator
     */
    public static byte[] makeAuthen(byte[] baseBuf, byte[] attr,
            byte[] sharedSecret) {
        byte resultBuf[] = new byte[16];// 返回结果
        // md5原始数据
        byte[] tempBuf = new byte[baseBuf.length + 16 + attr.length + sharedSecret.length];
        // 拷贝基础数据
        for (int i = 0; i < baseBuf.length; i++) {
            tempBuf[i] = baseBuf[i];
        }
        // 增加16字节0
        for (int i = 0; i < 16; i++) {
            tempBuf[baseBuf.length + i] = (byte) 0;
        }
        // attr
        if (attr.length > 0) {
            for (int i = 0; i < attr.length; i++) {
                tempBuf[baseBuf.length + 16 + i] = attr[i];
            }
            // sharedSecret共享密钥
            for (int i = 0; i < sharedSecret.length; i++) {
                tempBuf[baseBuf.length + 16 + attr.length + i] = sharedSecret[i];
            }
        } else {
            for (int i = 0; i < sharedSecret.length; i++) {
                tempBuf[baseBuf.length + 16 + i] = sharedSecret[i];
            }
        }

        // 生成Chap-Password
        try {
            /**
             * MessageDigest使用说明：
             * MessageDigest 通过其getInstance系列静态函数来进行实例化和初始化。 MessageDigest 对象通过使用
             * update 方法处理数据。任何时候都可以调用 reset 方法重置摘要。 一旦所有需要更新的数据都已经被更新了，应该调用 digest
             * 方法之一完成哈希计算并返回结果。 对于给定数量的更新数据，digest 方法只能被调用一次。digest
             * 方法被调用后，MessageDigest 对象被重新设置成其初始状态。
             */
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(tempBuf);
            resultBuf = md.digest();
            log.debug("生成Request Authenticator "
                        + PortalUtil.Getbyte2HexString(resultBuf));
        } catch (NoSuchAlgorithmException e) {
            log.debug("生成Request Authenticator出错！");

        }
        return resultBuf;
    }

    public static byte[] makeChapPassword(byte[] reqId, byte[] challenge,
            byte[] userPassword) {
        byte resultBuf[] = new byte[16];
        byte[] tempBuf = new byte[1 + userPassword.length + challenge.length];
        /*
         * Chap_Password的生成：Chap_Password的生成遵循标准的Radious协议中的Chap_Password
         * 生成方法（参见RFC2865）。 密码加密使用MD5算法，MD5函数的输入为ChapID ＋ Password ＋challenge
         * 其中，ChapID取ReqID的低 8 位，Password的长度不够协议规定的最大长度，其后不需要补零。
         */
        tempBuf[0] = reqId[1];
        for (int i = 0; i < userPassword.length; i++) {
            tempBuf[1 + i] = userPassword[i];
        }
        for (int i = 0; i < challenge.length; i++) {
            tempBuf[1 + userPassword.length + i] = challenge[i];
        }
        // 生成Chap-Password
        /**
         * MessageDigest 通过其getInstance系列静态函数来进行实例化和初始化。 MessageDigest 对象通过使用
         * update 方法处理数据。任何时候都可以调用 reset 方法重置摘要。 一旦所有需要更新的数据都已经被更新了，应该调用 digest
         * 方法之一完成哈希计算并返回结果。 对于给定数量的更新数据，digest 方法只能被调用一次。digest
         * 方法被调用后，MessageDigest 对象被重新设置成其初始状态。
         */
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(tempBuf);
            resultBuf = md.digest();
            log.debug("生成Chap-Password " + PortalUtil.Getbyte2HexString(resultBuf));
        } catch (NoSuchAlgorithmException e) {
            log.debug("生成Chap-Password出错！");
        }
        return resultBuf;
    }

	public static byte[] makeAckAuthen(byte[] Buff, byte[] Attrs,
								   String sharedSecret,byte[] reqAuthen) {
		byte[] Secret = sharedSecret.getBytes();
		byte Authen[] = new byte[16];
		// 初始化buf byte[]
		byte[] buf = new byte[Buff.length + 16 + Attrs.length + Secret.length];
		// 给buf byte[] 传值
		for (int i = 0; i < Buff.length; i++) {
			buf[i] = Buff[i];
		}
		for (int i = 0; i < 16; i++) {
			buf[Buff.length + i] = reqAuthen[i];
		}
		if (Attrs.length > 0) {
			for (int i = 0; i < Attrs.length; i++) {
				buf[Buff.length + 16 + i] = Attrs[i];
			}
			for (int i = 0; i < Secret.length; i++) {
				buf[Buff.length + 16 + Attrs.length + i] = Secret[i];
			}
		} else {
			for (int i = 0; i < Secret.length; i++) {
				buf[Buff.length + 16 + i] = Secret[i];
			}
		}
		// 生成Chap-Password
		/**
		 * MessageDigest 通过其getInstance系列静态函数来进行实例化和初始化。 MessageDigest 对象通过使用
		 * update 方法处理数据。任何时候都可以调用 reset 方法重置摘要。 一旦所有需要更新的数据都已经被更新了，应该调用 digest
		 * 方法之一完成哈希计算并返回结果。 对于给定数量的更新数据，digest 方法只能被调用一次。digest
		 * 方法被调用后，MessageDigest 对象被重新设置成其初始状态。
		 */
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(buf);
			Authen = md.digest();
			log.info("生成Request Authenticator :"
					+ PortalUtil.Getbyte2HexString(Authen));

		} catch (NoSuchAlgorithmException e) {
			log.info("生成Request Authenticator出错！");
		}
		return Authen;
	}

	public static void main(String[] args) {
		System.out.println(":" + new DateTime().plusMinutes(60).toDate());
	}
}
