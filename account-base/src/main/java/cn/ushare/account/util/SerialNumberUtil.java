package cn.ushare.account.util;


import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SerialNumberUtil {
   
   /**
    * 获取主板序列号
    * @return
    */
   public static String getMotherboardSN(String tempFilePath) {
       String result = "";
       try {
           File file = new File(tempFilePath + "\\mainboard.vbs");
           file.deleteOnExit();
           FileWriter fw = new java.io.FileWriter(file);

           String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                   + "Set colItems = objWMIService.ExecQuery _ \n"
                   + "   (\"Select * from Win32_BaseBoard\") \n"
                   + "For Each objItem in colItems \n"
                   + "    Wscript.Echo objItem.SerialNumber \n"
                   + "    exit for  ' do the first cpu only! \n" + "Next \n";

           fw.write(vbs);
           fw.close();
           String path = file.getPath().replace("%20", " ");
           Process p = Runtime.getRuntime().exec(
                   "cscript //NoLogo " + path);
           BufferedReader input = new BufferedReader(new InputStreamReader(
                   p.getInputStream()));
           String line;
           while ((line = input.readLine()) != null) {
               result += line;
           }
           input.close();
       } catch (Exception e) {
           e.printStackTrace();
       }
       return result.trim();
   }

   /**
    * 获取硬盘序列号(该方法获取的是 盘符的逻辑序列号,并不是硬盘本身的物理序列号)
    * @param drive 盘符
    * @return
    */
   public static String getHardDiskSN(String drive, String tempFilePath) {
       String result = "";
       try {
           File file = new File(tempFilePath + "\\harddisk.vbs");
           file.deleteOnExit();
           FileWriter fw = new java.io.FileWriter(file);

           String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                   + "Set colDrives = objFSO.Drives\n"
                   + "Set objDrive = colDrives.item(\""
                   + drive
                   + "\")\n"
                   + "Wscript.Echo objDrive.SerialNumber"; // see note
           fw.write(vbs);
           fw.close();
           String path = file.getPath().replace("%20", " ");
           Process p = Runtime.getRuntime().exec(
                   "cscript //NoLogo " + path);
           BufferedReader input = new BufferedReader(new InputStreamReader(
                   p.getInputStream()));
           String line;
           while ((line = input.readLine()) != null) {
               result += line;
           }
           input.close();
       } catch (Exception e) {
           e.printStackTrace();
       }
       return result.trim();
   }

   /**
    * 获取CPU序列号
    * @return
    */
   public static String getCPUSerial(String tempFilePath) {
       /*String result = "";
       try {
           File file = new File(tempFilePath + "\\cpu.vbs");
           file.deleteOnExit();
           FileWriter fw = new java.io.FileWriter(file);
           String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                   + "Set colItems = objWMIService.ExecQuery _ \n"
                   + "   (\"Select * from Win32_Processor\") \n"
                   + "For Each objItem in colItems \n"
                   + "    Wscript.Echo objItem.ProcessorId \n"
                   + "    exit for  ' do the first cpu only! \n" + "Next \n";

           // + "    exit for  \r\n" + "Next";
           fw.write(vbs);
           fw.close();
           String path = file.getPath().replace("%20", " ");
           Process p = Runtime.getRuntime().exec(
                   "cscript //NoLogo " + path);
           BufferedReader input = new BufferedReader(new InputStreamReader(
                   p.getInputStream()));
           String line;
           while ((line = input.readLine()) != null) {
               result += line;
           }
           input.close();
           file.delete();
       } catch (Exception e) {
           e.fillInStackTrace();
       }
       if (result.trim().length() < 1 || result == null) {
           result = "无CPU_ID被读取";
       }
       return result.trim();*/
       // linux，windows命令
       //todo edited by 李吉祥@2022-10-17 12:58 优化获取CPU信息代码
       String[] linux = {"dmidecode", "-t", "processor", "|", "grep", "'ID'"};
       String[] windows = {"wmic", "cpu", "get", "ProcessorId"};

       // 获取系统信息
       String property = System.getProperty("os.name");
       Process process = null;
       try {
           process = Runtime.getRuntime().exec(property.contains("Window") ? windows : linux);
           process.getOutputStream().close();
       } catch (IOException e) {
           e.printStackTrace();
       }
       Scanner sc = new Scanner(process.getInputStream(), "utf-8");
       sc.next();
       return sc.next();
   }
   
   private static List<String> getLocalHostLANAddress()    throws UnknownHostException, SocketException {
       List<String> ips = new ArrayList<String>();
       Enumeration<NetworkInterface> interfs = NetworkInterface.getNetworkInterfaces();
       while (interfs.hasMoreElements()) {
           NetworkInterface interf = interfs.nextElement();
           Enumeration<InetAddress> addres = interf.getInetAddresses();
           while (addres.hasMoreElements()) {
               InetAddress in = addres.nextElement();
               if (in instanceof Inet4Address) {
                   System.out.println("v4:" + in.getHostAddress());
                   if(!"127.0.0.1".equals(in.getHostAddress())){
                       ips.add(in.getHostAddress());
                   }
               }
           }
       }
       return ips;
   }
   
   /**
    * MAC
    * 通过jdk自带的方法,先获取本机所有的ip,然后通过NetworkInterface获取mac地址
    * @return
    */
   public static String getMac() {
       try {
           String resultStr = "";
           List<String> ls = getLocalHostLANAddress();
           for(String str : ls){
               InetAddress ia = InetAddress.getByName(str);// 获取本地IP对象
               // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
               byte[] mac = NetworkInterface.getByInetAddress(ia)
                       .getHardwareAddress();
               // 下面代码是把mac地址拼装成String
               StringBuilder sb = new StringBuilder();
               for (int i = 0; i < mac.length; i++) {
                   if (i != 0) {
                       sb.append("-");
                   }
                   // mac[i] & 0xFF 是为了把byte转化为正整数
                   String s = Integer.toHexString(mac[i] & 0xFF);
                   sb.append(s.length() == 1 ? 0 + s : s);
               }
               // 把字符串所有小写字母改为大写成为正规的mac地址并返回
               resultStr += sb.toString().toUpperCase()+",";
           }
           return resultStr;
       } catch (Exception ex) {
           ex.printStackTrace();
       }
       return null;
   }
   
   /***************************linux*********************************/

   public static String executeLinuxCmd(String cmd)  {
       try {
           log.info("got cmd job : " + cmd);
           Runtime run = Runtime.getRuntime();
           Process process;
           process = run.exec(cmd);
           InputStream in = process.getInputStream();
           BufferedReader bs = new BufferedReader(new InputStreamReader(in));
           StringBuffer out = new StringBuffer();
           byte[] b = new byte[8192];
           for (int n; (n = in.read(b)) != -1;) {
               out.append(new String(b, 0, n));
           }
           
           in.close();
           process.destroy();
           return out.toString();
       } catch (Exception e) {
           e.printStackTrace();
           log.error("serialNumberUtil error: ", e);
       }
       return null;
   }
   
   /**
    * 
    * @param cmd 命令语句
    * @param record 要查看的字段
    * @param symbol 分隔符
    * @return
    */
   public static String getSerialNumber(String cmd ,String record,String symbol) {
       String execResult = executeLinuxCmd(cmd);
       if (execResult == null) {
           return null;
       }
       String[] infos = execResult.split("\n");
       
       for(String info : infos) {
           info = info.trim();
           if(info.indexOf(record) != -1) {
               info.replace(" ", "");
               String[] sn = info.split(symbol);
               return sn[1];
           }
       }
       
       return null;
   }
   
   /**
    * 获取CPUID、硬盘序列号、MAC地址、主板序列号
    * @return
    */
   public static Map<String, String> getAllSn(String tempFilePath){
       String os = System.getProperty("os.name");
       os = os.toUpperCase();
       Map<String, String> snVo = new HashMap<String, String>();
       if("LINUX".equals(os)) {
           System.out.println("=============>for linux");
           String cpuid =              getSerialNumber("dmidecode -t processor | grep 'ID'", "ID",":");
           //System.out.println("cpuid : "+ cpuid);
           String mainboardNumber =    getSerialNumber("dmidecode |grep 'Serial Number'", "Serial Number",":");
           //System.out.println("mainboardNumber : "+ mainboardNumber);
           //String diskNumber =         getSerialNumber("fdisk -l", "Disk identifier",":");
           //System.out.println("diskNumber : "+ diskNumber);
           //String mac =                getSerialNumber("ifconfig -a", "ether"," ");
           
           snVo.put("cpuId",cpuid.toUpperCase().replace(" ", ""));
           //snVo.put("diskId",diskNumber.toUpperCase().replace(" ", ""));
           //snVo.put("mac",mac.toUpperCase().replace(" ", ""));
           snVo.put("mainboardId",mainboardNumber.toUpperCase().replace(" ", ""));
       }else {
           System.out.println("=============>for windows");
           String cpuid = SerialNumberUtil.getCPUSerial(tempFilePath);
           String mainboard = SerialNumberUtil.getMotherboardSN(tempFilePath);
           //String disk = SerialNumberUtil.getHardDiskSN("c", tempFilePath);
           //String mac = SerialNumberUtil.getMac();
           
           //System.out.println("CPU  SN:" + cpuid);
           //System.out.println("主板  SN:" + mainboard);
           //System.out.println("C盘   SN:" + disk);
           //System.out.println("MAC  SN:" + mac);
           
           snVo.put("cpuId",cpuid.toUpperCase().replace(" ", ""));
           //snVo.put("diskId",disk.toUpperCase().replace(" ", ""));
           //snVo.put("mac",mac.toUpperCase().replace(" ", ""));
           snVo.put("mainboardId",mainboard.toUpperCase().replace(" ", ""));
       }
       
       return snVo;
   }
   /**
    * linux
    * cpuid : dmidecode -t processor | grep 'ID'
    * mainboard : dmidecode |grep 'Serial Number'
    * disk : fdisk -l
    * mac : ifconfig -a
    * @param args
    */
//   public static void main(String[] args) {
//       getAllSn();
//   }
}
