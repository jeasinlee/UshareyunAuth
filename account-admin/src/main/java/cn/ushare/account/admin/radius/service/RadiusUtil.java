package cn.ushare.account.admin.radius.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.Vector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RadiusUtil {

	// 加入Framed-IP-Address(为用户提供的IP地址)参数
	public static String getAttributeIP(String ip) {
		byte[] UserIP = new byte[4];
		String[] ips = ip.split("[.]");
		// 将ip地址加入字段UserIP
		for (int i = 0; i < 4; i++) {
			int m = Integer.valueOf(ips[i]);
			byte b = (byte) m;
			UserIP[i] = b;
		}
		String info = ByteToHex(UserIP);

		String ret = null;
		try {

			String len = Integer.toHexString(2 + 4);
			while (len.length() < 2)
				len = "0" + len;
			String types = Integer.toHexString(8);
			while (types.length() < 2)
				types = "0" + types;
			ret = types + len + info;
		} catch (Exception e) {
		}
		return ret;
	}
	// 加入NAS-IP参数
	public static String getAttributeNasIP(String ip) {
		byte[] UserIP = new byte[4];
		String[] ips = ip.split("[.]");
		// 将ip地址加入字段UserIP
		for (int i = 0; i < 4; i++) {
			int m = Integer.valueOf(ips[i]);
			byte b = (byte) m;
			UserIP[i] = b;
		}
		String info = ByteToHex(UserIP);

		String ret = null;
		try {

			String len = Integer.toHexString(2 + 4);
			while (len.length() < 2)
				len = "0" + len;
			String types = Integer.toHexString(4);
			while (types.length() < 2)
				types = "0" + types;
			ret = types + len + info;
		} catch (Exception e) {
		}
		return ret;
	}

	// 打开代理服务器
	public static void openProxy(String proxyhost, String proxyport) {
		try {
			System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
			System.setProperty("sun.net.client.defaultReadTimeout", "10000");
			if ((proxyhost != null) && (proxyport != null)) {
				Properties prop = System.getProperties();
				prop.put("http.proxyHost", proxyhost);
				prop.put("http.proxyPort", proxyport);
			}
		} catch (Exception e) {
		}
	}

	// 发送URL
	public static String sendURL(String url) {
		StringBuffer ret = new StringBuffer();
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			URL su = new URL(url);
			is = su.openStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			for (String line = br.readLine(); line != null; line = br
					.readLine())
				ret.append(line + "\r\n");
		} catch (Exception e) {
		}
		try {
			if (br != null) {
				br.close();
				br = null;
			}
		} catch (Exception e) {
		}
		try {
			if (isr != null) {
				isr.close();
				isr = null;
			}
		} catch (Exception e) {
		}
		try {
			if (is != null) {
				is.close();
				is = null;
			}
		} catch (Exception e) {
		}
		return ret.toString();
	}

	// MD5加密
	public static String encodeMD5(String str) {
		String ret = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(HexToByte(str));
			// ret=new String(md.digest());
			ret = ByteToHex(md.digest());
		} catch (Exception e) {
		}
		return ret;
	}

	// sharedSecret和authenticator用password进行MD5加密
	public static String secretAuthenMD5(String sharedSecret, String authenticator,
			String password) {
		String ret = "";
		try {
			byte[] pwd = HexToByte(password);
			if ((pwd != null) && (pwd.length >= 16)) {
				// sharedSecret = ByteToHex(sharedSecret.getBytes());
				byte[] temp = HexToByte(encodeMD5(sharedSecret + authenticator));
				for (int i = 0; i < 16; i++) {
					pwd[i] = (byte) (temp[i] ^ pwd[i]);
				}
				if (pwd.length > 16) {
					for (int i = 16; i < pwd.length; i += 16) {
						temp = HexToByte(encodeMD5(sharedSecret
								+ ByteToHex(pwd, i - 16, 16)));
						for (int j = 0; j < 16; j++) {
							pwd[i + j] = (byte) (temp[j] ^ pwd[i + j]);
						}
					}
				}
				int len = pwd.length;
				while ((len > 0) && (pwd[len - 1] == 0))
					len--;
				ret = new String(pwd, 0, len);
			}
		} catch (Exception e) {
		}
		return ret;
	}

	public static String ByteToHex(byte[] buf) {
		StringBuffer ret = new StringBuffer();
		String result=null;
		try {
			for (int i = 0; i < buf.length; i++) {
				String temp = Integer.toHexString(buf[i] & 0xff);
				while (temp.length() < 2)
					temp = "0" + temp;
				ret.append(temp);
			}
//			result=ret.toString().toUpperCase();
			result=ret.toString();
		} catch (Exception e) {
		}
		return result;
	}

	public static String ByteToHex(byte[] buf, int begin, int end) {
		StringBuffer ret = new StringBuffer();
		String result=null;
		try {
			for (int i = begin; i < end; i++) {
				String temp = Integer.toHexString(buf[i] & 0xff);
				while (temp.length() < 2)
					temp = "0" + temp;
				ret.append(temp);
			}
//			result=ret.toString().toUpperCase();
			result=ret.toString();
		} catch (Exception e) {
		}
		return result;
	}

	public static byte[] HexToByte(String value) {
		byte[] ret = null;
		try {
			int len = value.length() / 2;
			ret = new byte[len];
			for (int i = 0; i < len; i++) {
				ret[i] = (byte) Integer.parseInt(
						value.substring(i * 2, i * 2 + 2), 16);
			}
		} catch (Exception e) {
		}
		return ret;
	}

	public static long ByteToLong(byte[] value) {
		long ret = 0;
		try {
			for (int i = 0; i < value.length; i++) {
				ret = (ret << 8) | (value[i] & 0xff);
			}
		} catch (Exception e) {
		}
		return ret;
	}

	public static int ByteToInt(byte[] value) {
		int ret = value[0] & 0xff;
		return ret;
	}

	public static String ByteToIP(byte[] value) {
		String ret = null;
		try {
			if ((value != null) && (value.length >= 4)) {
				// ret = ((value[0] >= 0) ? value[0] : 255) + "."
				// + ((value[1] >= 0) ? value[1] : 255) + "."
				// + ((value[2] >= 0) ? value[2] : 255) + "."
				// + ((value[3] >= 0) ? value[3] : 255);

				ret = new StringBuffer().append(value[0] & 0xFF).append('.')
						.append(value[1] & 0xFF).append('.')
						.append(value[2] & 0xFF).append('.')
						.append(value[3] & 0xFF).toString();
			}
		} catch (Exception e) {
		}
		return ret;
	}

	/**
	 * 16进制转换成为string类型字符串
	 * @param s
	 * @return
	 */
	public static String hexStringToString(String s) {
		if (s == null || s.equals("")) {
			return null;
		}
		s = s.replace(" ", "");
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "UTF-8");
			new String();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	/**
	 * 属性域解析成list，每行item[0]为type，item[1]为value
	 * 属性域格式：type(1字节)、length(1字节)、value(不定长)
	 */
	public static String[][] getAttributes(String attributes) {
		String[][] ret = null;
		Vector<String> list = null;
		try {
			list = new Vector<String>();
			while (attributes.length() >= 4) {
				int len = 4;
				try {
					len = Integer.parseInt(attributes.substring(2, 4), 16) * 2;
					if (len >= 4) {
						list.add(attributes.substring(0, len));
					} else {
						len = 4;
					}
				} catch (Exception e1) {
				}
				attributes = attributes.substring(len);
			}
		} catch (Exception e) {
		}
		try {
			if ((list != null) && (list.size() > 0)) {
				int len = list.size();
				ret = new String[len][2];
				for (int i = 0; i < len; i++) {
					try {
						String temp = list.get(i);
						ret[i][0] = temp.substring(0, 2);// type值
						ret[i][1] = temp.substring(4);// value值
					} catch (Exception e1) {
					}
				}
				list.clear();
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// value有4种类型：
	// String 0~253字节，字符串
	// Ipaddress 4字节，IP地址
	// Integer 4字节，整数
	// Time 4字节，从1970-01-01 00:00:00到当前的总秒数
	public static String getAttributeValue(String ip, int type, String value) {
		String ret = null;
		String name = null;
		try {
			switch (type) {
			case 1:// User-Name(用户账户ID)
			{
				name = "User-Name";
				ret = new String(HexToByte(value));
			}
				break;
			case 2:// User-Password(用户密码)
			{
				name = "User-Password";
				ret = value;
			}
				break;
			case 3:// Chap-Password
			{
				name = "Chap-Password";
				ret = value;
			}
				break;
			case 4:// Nas-IP-Address(Nas的ip地址)
			{
				name = "Nas-IP-Address";
				ret = ByteToIP(HexToByte(value));
			}
				break;
			case 5:// Nas-Port(用户接入端口号)
			{
				name = "Nas-Port";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 6:// Service-Type(服务类型)
			{
				name = "Service-Type";
				ret = getServiceType((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 7:// Framed-Protocol(协议类型)
			{
				name = "Framed-Protocol";
				ret = getFramedProtocol((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 8:// Framed-IP-Address(为用户提供的IP地址)
			{
				name = "Framed-IP-Address";
				ret = ByteToIP(HexToByte(value));
			}
				break;
			case 9:// Framed-IP-Netmask
			{
				name = "Framed-IP-Netmask";
				ret = ByteToIP(HexToByte(value));
			}
				break;
			case 10:// Framed-Routing
			{
				name = "Framed-Routing";
				ret = getFramedRouting((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 11:// Filter-Id(过滤表的名称)
			{
				name = "Filter-Id";
				ret = new String(HexToByte(value));
			}
				break;
			case 12:// Framed-MTU
			{
				name = "Framed-MTU";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 13:// Framed-Compression
			{
				name = "Framed-Compression";
				ret = getFramedCompression((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 14:// Login-IP-Host
			{
				name = "Login-IP-Host";
				ret = ByteToIP(HexToByte(value));
			}
				break;
			case 15:// Login-Service
			{
				name = "Login-Service";
				ret = getLoginService((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 16:// Login-TCP-Port
			{
				name = "Login-TCP-Port";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 18:// Reply-Message
			{
				name = "Reply-Message";
				ret = new String(HexToByte(value));
			}
				break;
			case 19:// Callback-Number
			{
				name = "Callback-Number";
				ret = new String(HexToByte(value));
			}
				break;
			case 20:// Callback-Id
			{
				name = "Callback-Id";
				ret = new String(HexToByte(value));
			}
				break;
			case 22:// Framed-Route
			{
				name = "Framed-Route";
				ret = new String(HexToByte(value));
			}
				break;
			case 23:// Framed-IPX-Network
			{
				name = "Framed-IPX-Network";
				ret = ByteToIP(HexToByte(value));
			}
				break;
			case 24:// State
			{
				name = "State";
				ret = value;
			}
				break;
			case 25:// Class
			{
				name = "Class";
				ret = value;
			}
				break;
			case 26:// Vendor-Specific
			{
				name = "Vendor-Specific";
				ret = value;
			}
				break;
			case 27:// Session-Timeout(通知NAS该用户可用的会话时长（时长预付费）)
			{
				name = "Session-Timeout";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 28:// Idle-Timeout
			{
				name = "Idle-Timeout";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 29:// Termination-Action
			{
				name = "Termination-Action";
				ret = getTerminationAction((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 30:// Called-Station-Id
			{
				name = "Called-Station-Id";
				ret = new String(HexToByte(value));
			}
				break;
			case 31:// Calling-Station-Id
			{
				name = "Calling-Station-Id";
				ret = new String(HexToByte(value));
			}
				break;
			case 32:// Nas-Identifier(标识NAS的字符串)
			{
				name = "Nas-Identifier";
				ret = new String(HexToByte(value));
			}
				break;
			case 33:// Proxy-State
			{
				name = "Proxy-State";
				ret = value;
			}
				break;
			case 34:// Login-LAT-Service
			{
				name = "Login-LAT-Service";
				ret = new String(HexToByte(value));
			}
				break;
			case 35:// Login-LAT-Node
			{
				name = "Login-LAT-Node";
				ret = new String(HexToByte(value));
			}
				break;
			case 36:// Login-LAT-Group
			{
				name = "Login-LAT-Group";
				ret = value;
			}
				break;
			case 37:// Framed-AppleTalk-Link
			{
				name = "Framed-AppleTalk-Link";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 38:// Framed-AppleTalk-Network
			{
				name = "Framed-AppleTalk-Network";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 39:// Framed-AppleTalk-Zone
			{
				name = "Framed-AppleTalk-Zone";
				ret = new String(HexToByte(value));
			}
				break;
			case 40:// Acct-Status-Type(计费请求报文的类型)
			{
				name = "Acct-Status-Type";
				ret = getAcctStatusType((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 41:// Acct-Delay-Time(Radius客户端发送计费报文耗费的时间)
			{
				name = "Acct-Delay-Time";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 42:// Acct-Input-Octets(上行)
			{
				name = "Acct-Input-Octets";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 43:// Acct-Output-Octets(下行)
			{
				name = "Acct-Output-Octets";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 44:// Acct-Session-Id(计费会话标识)
			{
				name = "Acct-Session-Id";
				ret = new String(HexToByte(value));
			}
				break;
			case 45:// Acct-Authentic(在计费包中标识用户认证通过的方式)
			{
				name = "Acct-Authentic";
				ret = getAcctAuthentic((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 46:// Acct-Session-Time(通话时长(用户在线时长）)
			{
				name = "Acct-Session-Time";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 47:// Acct-Input-Packets
			{
				name = "Acct-Input-Packets";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 48:// Acct-Output-Packets
			{
				name = "Acct-Output-Packets";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 49:// Acct-Terminate-Cause(用户下线原因)
			{
				name = "Acct-Terminate-Cause";
				ret = getAcctTerminateCause((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 50:// Acct-Multi-Session-Id
			{
				name = "Acct-Multi-Session-Id";
				ret = new String(HexToByte(value));
			}
				break;
			case 51:// Acct-Link-Count
			{
				name = "Acct-Link-Count";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 52:// Acct-Input-Gigawords
			{
				name = "Acct-Input-Gigawords";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 53:// Acct-Output-Gigawords
			{
				name = "Acct-Output-Gigawords";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 55:// Event-Timestamp
			{
				name = "Event-Timestamp";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 60:// CHAP-Challenge
			{
				name = "CHAP-Challenge";
				ret = value;
			}
				break;
			case 61:// NAS-Port-Type
			{
				name = "NAS-Port-Type";
				ret = getNASPortType((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 62:// Port-Limit
			{
				name = "Port-Limit";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 63:// Login-LAT-Port
			{
				name = "Login-LAT-Port";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 68:// Acct-Tunnel-Connection
			{
				name = "Acct-Tunnel-Connection";
				ret = new String(HexToByte(value));
			}
				break;
			case 70:// ARAP-Password
			{
				name = "ARAP-Password";
				ret = new String(HexToByte(value));
			}
				break;
			case 71:// ARAP-Features
			{
				name = "ARAP-Features";
				ret = new String(HexToByte(value));
			}
				break;
			case 72:// ARAP-Zone-Access
			{
				name = "ARAP-Zone-Access";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 73:// ARAP-Security
			{
				name = "ARAP-Security";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 74:// ARAP-Security-Data
			{
				name = "ARAP-Security-Data";
				ret = new String(HexToByte(value));
			}
				break;
			case 75:// Password-Retry
			{
				name = "Password-Retry";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 76:// Prompt
			{
				name = "Prompt";
				ret = getPrompt((int) ByteToLong(HexToByte(value)));
			}
				break;
			case 77:// Connect-Info
			{
				name = "Connect-Info";
				ret = new String(HexToByte(value));
			}
				break;
			case 78:// Configuration-Token
			{
				name = "Configuration-Token";
				ret = new String(HexToByte(value));
			}
				break;
			case 79:// EAP-Message
			{
				name = "EAP-Message";
				ret = value;
			}
				break;
			case 80:// Message-Authenticator
			{
				name = "Message-Authenticator";
				ret = value;
			}
				break;
			case 84:// ARAP-Challenge-Response
			{
				name = "ARAP-Challenge-Response";
				ret = new String(HexToByte(value));
			}
				break;
			case 85:// Acct-Interim-Interval 计费更新报文的时间间隔 (以秒为单位)
			{
				name = "Acct-Interim-Interval";
				ret = Long.toString(ByteToLong(HexToByte(value)));
			}
				break;
			case 87:// NAS-Port-Id
			{
				name = "NAS-Port-Id";
				ret = new String(HexToByte(value));
			}
				break;
			case 88:// Framed-Pool
			{
				name = "Framed-Pool";
				ret = new String(HexToByte(value));
			}
				break;
			case 95:// NAS-IPv6-Address
			{
				name = "NAS-IPv6-Address";
				ret = value;
			}
				break;
			case 96:// Framed-Interface-Id
			{
				name = "Framed-Interface-Id";
				ret = value;
			}
				break;
			case 97:// Framed-IPv6-Prefix
			{
				name = "Framed-IPv6-Prefix";
				ret = value;
			}
				break;
			case 98:// Login-IPv6-Host
			{
				name = "Login-IPv6-Host";
				ret = value;
			}
				break;
			case 99:// Framed-IPv6-Route
			{
				name = "Framed-IPv6-Route";
				ret = new String(HexToByte(value));
			}
				break;
			case 100:// Framed-IPv6-Pool
			{
				name = "Framed-IPv6-Pool";
				ret = new String(HexToByte(value));
			}
				break;
			case 206:// Digest-Response
			{
				name = "Digest-Response";
				ret = new String(HexToByte(value));
			}
				break;
			case 207:// Digest-Attributes
			{
				name = "Digest-Attributes";
				ret = value;
			}
				break;
			default: {
				name = "";
				ret = value;
			}
				break;
			}
		} catch (Exception e) {
		}
		if (ret == null)
			ret = value;
		//log.debug(ip + ">> " + name + "(" + type + ")=" + ret);
		return ret;
	}

	private static String getServiceType(int value) {
		String ret = null;
		try {
			switch (value) {
			case 1:// Login-User
			{
				ret = "Login-User";
			}
				break;
			case 2:// Framed-User
			{
				ret = "Framed-User";
			}
				break;
			case 3:// Callback-Login-User
			{
				ret = "Callback-Login-User";
			}
				break;
			case 4:// Callback-Framed-User
			{
				ret = "Callback-Framed-User";
			}
				break;
			case 5:// Outbound-User
			{
				ret = "Outbound-User";
			}
				break;
			case 6:// Administrative-User
			{
				ret = "Administrative-User";
			}
				break;
			case 7:// NAS-Prompt-User
			{
				ret = "NAS-Prompt-User";
			}
				break;
			case 8:// Authenticate-Only
			{
				ret = "Authenticate-Only";
			}
				break;
			case 9:// Callback-NAS-Prompt
			{
				ret = "Callback-NAS-Prompt";
			}
				break;
			case 10:// Call-Check
			{
				ret = "Call-Check";
			}
				break;
			case 11:// Callback-Administrative
			{
				ret = "Callback-Administrative";
			}
				break;
			case 12:// Voice
			{
				ret = "Voice";
			}
				break;
			case 13:// Fax
			{
				ret = "Fax";
			}
				break;
			case 14:// Modem-Relay
			{
				ret = "Modem-Relay";
			}
				break;
			case 15:// IAPP-Register
			{
				ret = "IAPP-Register";
			}
				break;
			case 16:// IAPP-AP-Check
			{
				ret = "IAPP-AP-Check";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getFramedProtocol(int value) {
		String ret = null;
		try {
			switch (value) {
			case 1:// PPP
			{
				ret = "PPP";
			}
				break;
			case 2:// SLIP
			{
				ret = "SLIP";
			}
				break;
			case 3:// ARAP
			{
				ret = "ARAP";
			}
				break;
			case 4:// Gandalf-SLML
			{
				ret = "Gandalf-SLML";
			}
				break;
			case 5:// Xylogics-IPX-SLIP
			{
				ret = "Xylogics-IPX-SLIP";
			}
				break;
			case 6:// X.75-Synchronous
			{
				ret = "X.75-Synchronous";
			}
				break;
			case 7:// GPRS-PDP-Context
			{
				ret = "GPRS-PDP-Context";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getFramedRouting(int value) {
		String ret = null;
		try {
			switch (value) {
			case 0:// None
			{
				ret = "None";
			}
				break;
			case 1:// Broadcast
			{
				ret = "Broadcast";
			}
				break;
			case 2:// Listen
			{
				ret = "Listen";
			}
				break;
			case 3:// Broadcast-Listen
			{
				ret = "Broadcast-Listen";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getFramedCompression(int value) {
		String ret = null;
		try {
			switch (value) {
			case 0:// None
			{
				ret = "None";
			}
				break;
			case 1:// Van-Jacobson-TCP-IP
			{
				ret = "Van-Jacobson-TCP-IP";
			}
				break;
			case 2:// IPX-Header-Compression
			{
				ret = "IPX-Header-Compression";
			}
				break;
			case 3:// Stac-LZS
			{
				ret = "Stac-LZS";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getLoginService(int value) {
		String ret = null;
		try {
			switch (value) {
			case 0:// Telnet
			{
				ret = "Telnet";
			}
				break;
			case 1:// Rlogin
			{
				ret = "Rlogin";
			}
				break;
			case 2:// TCP-Clear
			{
				ret = "TCP-Clear";
			}
				break;
			case 3:// PortMaster
			{
				ret = "PortMaster";
			}
				break;
			case 4:// LAT
			{
				ret = "LAT";
			}
				break;
			case 5:// X25-PAD
			{
				ret = "X25-PAD";
			}
				break;
			case 6:// X25-T3POS
			{
				ret = "X25-T3POS";
			}
				break;
			case 7:// TCP-Clear-Quiet
			{
				ret = "TCP-Clear-Quiet";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getAcctStatusType(int value) {
		String ret = null;
		try {
			switch (value) {
			case 1:// Start
			{
				ret = "Start";
			}
				break;
			case 2:// Stop
			{
				ret = "Stop";
			}
				break;
			case 3:// Interim-Update
			{
				ret = "Interim-Update";
			}
				break;
			case 4:// Alive
			{
				ret = "Alive";
			}
				break;
			case 7:// Accounting-On
			{
				ret = "Accounting-On";
			}
				break;
			case 8:// Accounting-Off
			{
				ret = "Accounting-Off";
			}
				break;
			case 9:// Tunnel-Start
			{
				ret = "Tunnel-Start";
			}
				break;
			case 10:// Tunnel-Stop
			{
				ret = "Tunnel-Stop";
			}
				break;
			case 11:// Tunnel-Reject
			{
				ret = "Tunnel-Reject";
			}
				break;
			case 12:// Tunnel-Link-Start
			{
				ret = "Tunnel-Link-Start";
			}
				break;
			case 13:// Tunnel-Link-Stop
			{
				ret = "Tunnel-Link-Stop";
			}
				break;
			case 14:// Tunnel-Link-Reject
			{
				ret = "Tunnel-Link-Reject";
			}
				break;
			case 15:// Failed
			{
				ret = "Failed";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getAcctAuthentic(int value) {
		String ret = null;
		try {
			switch (value) {
			case 1:// RADIUS
			{
				ret = "RADIUS";
			}
				break;
			case 2:// Local
			{
				ret = "Local";
			}
				break;
			case 3:// Remote
			{
				ret = "Remote";
			}
				break;
			case 4:// Diameter
			{
				ret = "Diameter";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getTerminationAction(int value) {
		String ret = null;
		try {
			switch (value) {
			case 0:// Default
			{
				ret = "Default";
			}
				break;
			case 1:// RADIUS-Request
			{
				ret = "RADIUS-Request";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getNASPortType(int value) {
		String ret = null;
		try {
			switch (value) {
			case 0:// Async
			{
				ret = "Async";
			}
				break;
			case 1:// Sync
			{
				ret = "Sync";
			}
				break;
			case 2:// ISDN
			{
				ret = "ISDN";
			}
				break;
			case 3:// ISDN-V120
			{
				ret = "ISDN-V120";
			}
				break;
			case 4:// ISDN-V110
			{
				ret = "ISDN-V110";
			}
				break;
			case 5:// Virtual
			{
				ret = "Virtual";
			}
				break;
			case 6:// PIAFS
			{
				ret = "PIAFS";
			}
				break;
			case 7:// HDLC-Clear-Channel
			{
				ret = "HDLC-Clear-Channel";
			}
				break;
			case 8:// X.25
			{
				ret = "X.25";
			}
				break;
			case 9:// X.75
			{
				ret = "X.75";
			}
				break;
			case 10:// G.3-Fax
			{
				ret = "G.3-Fax";
			}
				break;
			case 11:// SDSL
			{
				ret = "SDSL";
			}
				break;
			case 12:// ADSL-CAP
			{
				ret = "ADSL-CAP";
			}
				break;
			case 13:// ADSL-DMT
			{
				ret = "ADSL-DMT";
			}
				break;
			case 14:// IDSL
			{
				ret = "IDSL";
			}
				break;
			case 15:// Ethernet
			{
				ret = "Ethernet";
			}
				break;
			case 16:// xDSL
			{
				ret = "xDSL";
			}
				break;
			case 17:// Cable
			{
				ret = "Cable";
			}
				break;
			case 18:// Wireless-Other
			{
				ret = "Wireless-Other";
			}
				break;
			case 19:// Wireless-802.11
			{
				ret = "Wireless-802.11";
			}
				break;
			case 20:// Token-Ring
			{
				ret = "Token-Ring";
			}
				break;
			case 21:// FDDI
			{
				ret = "FDDI";
			}
				break;
			case 22:// Wireless-CDMA2000
			{
				ret = "Wireless-CDMA2000";
			}
				break;
			case 23:// Wireless-UMTS
			{
				ret = "Wireless-UMTS";
			}
				break;
			case 24:// Wireless-1X-EV
			{
				ret = "Wireless-1X-EV";
			}
				break;
			case 25:// IAPP
			{
				ret = "IAPP";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getAcctTerminateCause(int value) {
		String ret = null;
		try {
			switch (value) {
			case 1:// User-Request
			{
				ret = "User-Request";
			}
				break;
			case 2:// Lost-Carrier
			{
				ret = "Lost-Carrier";
			}
				break;
			case 3:// Lost-Service
			{
				ret = "Lost-Service";
			}
				break;
			case 4:// Idle-Timeout
			{
				ret = "Idle-Timeout";
			}
				break;
			case 5:// Session-Timeout
			{
				ret = "Session-Timeout";
			}
				break;
			case 6:// Admin-Reset
			{
				ret = "Admin-Reset";
			}
				break;
			case 7:// Admin-Reboot
			{
				ret = "Admin-Reboot";
			}
				break;
			case 8:// Port-Error
			{
				ret = "Port-Error";
			}
				break;
			case 9:// NAS-Error
			{
				ret = "NAS-Error";
			}
				break;
			case 10:// NAS-Request
			{
				ret = "NAS-Request";
			}
				break;
			case 11:// NAS-Reboot
			{
				ret = "NAS-Reboot";
			}
				break;
			case 12:// Port-Unneeded
			{
				ret = "Port-Unneeded";
			}
				break;
			case 13:// Port-Preempted
			{
				ret = "Port-Preempted";
			}
				break;
			case 14:// Port-Suspended
			{
				ret = "Port-Suspended";
			}
				break;
			case 15:// Service-Unavailable
			{
				ret = "Service-Unavailable";
			}
				break;
			case 16:// Callback
			{
				ret = "Callback";
			}
				break;
			case 17:// User-Error
			{
				ret = "User-Error";
			}
				break;
			case 18:// Host-Request
			{
				ret = "Host-Request";
			}
				break;
			case 19:// Supplicant-Restart
			{
				ret = "Supplicant-Restart";
			}
				break;
			case 20:// Reauthentication-Failure
			{
				ret = "Reauthentication-Failure";
			}
				break;
			case 21:// Port-Reinit
			{
				ret = "Port-Reinit";
			}
				break;
			case 22:// Port-Disabled
			{
				ret = "Port-Disabled";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	private static String getPrompt(int value) {
		String ret = null;
		try {
			switch (value) {
			case 0:// No-Echo
			{
				ret = "No-Echo";
			}
				break;
			case 1:// Echo
			{
				ret = "Echo";
			}
				break;
			default: {
				ret = "";
			}
				break;
			}
			ret += "(" + value + ")";
		} catch (Exception e) {
		}
		return ret;
	}

	public static String getAttributeString(int type, String value) {
		String ret = null;
		try {
			String info = RadiusUtil.ByteToHex(value.getBytes());
			String len = Integer.toHexString(2 + info.length() / 2);
			while (len.length() < 2)
				len = "0" + len;
			String types = Integer.toHexString(type);
			while (types.length() < 2)
				types = "0" + types;
			ret = types + len + info;
		} catch (Exception e) {
		}
		return ret;
	}

	public static int getAttributeStringLen(String value) {
		int len = 0;
		try {
			String info = RadiusUtil.ByteToHex(value.getBytes());
			len = 2 + info.length() / 2;
		} catch (Exception e) {
		}
		return len;
	}


	//私有属性--start
	public static String getAttributeVendor(int type, int value, int valueLen)
	{
		String ret = null;
		try
		{
			byte[] b = new byte[4];
			for (int i = 0; i < 4; i++) {
				int offset = (b.length - 1 - i) * 8;
				b[i] = (byte) ((value >>> offset) & 0xFF);
			}
			String info=ByteToHex(b);
			String len = Integer.toHexString(2 + 4 + valueLen);
			while (len.length() < 2)
				len = "0" + len;
			String types = Integer.toHexString(type);
			while (types.length() < 2)
				types = "0" + types;
			ret = types + len + info;
		}
		catch (Exception e)
		{
		}
		return ret;
	}
	public static String getAttributeVendor(int type, int value)
	{
		String ret = null;
		try
		{
			byte[] b = new byte[4];
			for (int i = 0; i < 4; i++) {
				int offset = (b.length - 1 - i) * 8;
				b[i] = (byte) ((value >>> offset) & 0xFF);
			}
			String info=ByteToHex(b);
			String len = Integer.toHexString(2 + 4 + 6);
			while (len.length() < 2)
				len = "0" + len;
			String types = Integer.toHexString(type);
			while (types.length() < 2)
				types = "0" + types;
			ret = types + len + info;
		}
		catch (Exception e)
		{
		}
		return ret;
	}
	public static String getAttributeSpeed(int type, int value)
	{
		String ret = null;
		try
		{
			byte[] b = new byte[4];
			for (int i = 0; i < 4; i++) {
				int offset = (b.length - 1 - i) * 8;
				b[i] = (byte) ((value >>> offset) & 0xFF);
			}
			String info=ByteToHex(b);
			String len = Integer.toHexString(2 + 4);
			while (len.length() < 2)
				len = "0" + len;
			String types = Integer.toHexString(type);
			while (types.length() < 2)
				types = "0" + types;
			ret = types + len + info;
		}
		catch (Exception e)
		{
		}
		return ret;
	}
	//私有属性--end

	public static String getAttributeInt(int type, int value)
	{
		String ret = null;
		try
		{
			byte[] b = new byte[4];
		     for (int i = 0; i < 4; i++) {
		            int offset = (b.length - 1 - i) * 8;
		             b[i] = (byte) ((value >>> offset) & 0xFF);
		      }
		    String info=ByteToHex(b);
			String len = Integer.toHexString(2 + 4);
			while (len.length() < 2)
				len = "0" + len;
			String types = Integer.toHexString(type);
			while (types.length() < 2)
				types = "0" + types;
			ret = types + len + info;
		}
		catch (Exception e)
		{
		}
		return ret;
	}

	public static byte[] getOutData(String name, String sharedSecret,
			String ip, int port, int code, int identifier,
			String authenticator, String attributes) {
		byte[] ret = null;
		try {
			int length = 20 + attributes.length() / 2;
			String str = Integer.toHexString(code);
			while (str.length() < 2)
				str = "0" + str;
			String temp = Integer.toHexString(identifier);
			while (temp.length() < 2)
				temp = "0" + temp;
			str += temp;
			temp = Integer.toHexString(length);
			while (temp.length() < 4)
				temp = "0" + temp;
			str += temp;
			authenticator = encodeMD5(str + authenticator + attributes
					+ sharedSecret);
			str += authenticator;
			str += attributes;
			ret = HexToByte(str);
			log.debug("------------ radius build outData: name=" + name + " ip=" + ip + ",port=" + port + ",code=" + code
					+ ",identifier=" + identifier + ",length=" + length
					+ ",authenticator=" + authenticator + ",attributes="
					+ attributes);
		} catch (Exception e) {
		}
		return ret;
	}

	public static String sendServer(String ip, String url, String str) {
		String ret = null;
		try {
			str = url + "?" + str;
			String result = sendURL(str);
			//log.debug(ip, str);
			int i = result.indexOf("<result>");
			if (i >= 0) {
				i += 8;
				int j = result.indexOf("</result>", i);
				if (j > i) {
					ret = result.substring(i, j).trim();
					// 0 AUTHOK
					// 1 ERRORXML
					// 2 INVALIDUSER
					// 3 AUTHERROR
					// 4 PARAMETERERROR
					// 5 CLIENTIPRERROR
					// 6 APPSTOP
				}
			}
			//log.debug(ip + "result=" + ret);
		} catch (Exception e) {
		}
		return ret;
	}
}
