package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.SystemCmdService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.SerialNumberUtil;
import cn.ushare.account.util.SigarUtils;
import cn.ushare.account.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class SystemCmdServiceImpl implements SystemCmdService {

    private static final String dbName = "webauth-account";
    @Value("${spring.datasource.username}")
    String dbUserName;
    @Value("${spring.datasource.password}")
    String dbPassword;
    @Value("${path.sigarLibPath}")
    String sigarLibPath;
    @Value("${path.uploadPath}")
    String tempPath;
    @Value("${enviroment}")
    String enviroment;

    /**
     * 获取硬件编号
     */
    @Override
    public Map<String, String> getHardwareSn() {
        return SerialNumberUtil.getAllSn(tempPath);
    }

    @Override
    public BaseResult getSystemStatus() {

        Sigar sigar = new Sigar();
        long totalStorage = 0;
        long totalMem = 0;
        String sysName = null;
        String ip = null;
        FileSystem fslist[];
        try {

            //获取磁盘大小
            fslist = sigar.getFileSystemList();
            for (int i = 0; i < fslist.length; i++) {
                //System.out.println("分区的盘符名称" + i);
                FileSystem fs = fslist[i];
                FileSystemUsage usage = null;
                usage = sigar.getFileSystemUsage(fs.getDirName());
                switch (fs.getType()) {
                    case 0: // TYPE_UNKNOWN ：未知
                        break;
                    case 1: // TYPE_NONE
                        break;
                    case 2: // TYPE_LOCAL_DISK : 本地硬盘
                        totalStorage += usage.getTotal();
                        break;
                    case 3:// TYPE_NETWORK ：网络
                        break;
                    case 4:// TYPE_RAM_DISK ：闪存
                        break;
                    case 5:// TYPE_CDROM ：光驱
                        break;
                    case 6:// TYPE_SWAP ：页面交换
                        break;
                }
            }

            //获取总内存
            Mem mem = sigar.getMem();
            totalMem = mem.getTotal();

            //获取操作系统
            OperatingSystem OS = OperatingSystem.getInstance();
            sysName = OS.getName();

            //获取IP
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();

        } catch (SigarException | UnknownHostException e) {
            e.printStackTrace();
        } finally {

        }
        Map<String, Object> mapObj = new HashMap<>();
        mapObj.put("totalStorage", totalStorage);
        mapObj.put("totalMem", totalMem);
        mapObj.put("sysName", sysName);
        mapObj.put("ip", ip);

        return new BaseResult(mapObj);
    }

    /**
     * 获取os序列号
     */
    @Override
    public String getOsSerial() {
        String osName = System.getProperty("os.name");
        try {
            if (osName.matches("^(?i)Windows.*$")) {// Window 系统
                String osSerial = "";

                //使用WMIC获取主板序列号
                Process process = Runtime.getRuntime().exec("wmic bios get serialnumber");
                process.getOutputStream().close();
                Scanner scanner = new Scanner(process.getInputStream());

                if(scanner.hasNext()){
                    scanner.next();
                }

                if(scanner.hasNext()){
                    osSerial = scanner.next().trim();
                }

                scanner.close();
                return osSerial;
            } else {// Linux系统
                //序列号
                String osNumber = "";

                //使用dmidecode命令获取主板序列号
                String[] shell = {"/bin/bash","-c","dmidecode | grep 'UUID' | awk -F ':' '{print $2}' | head -n 1"};
                Process process = Runtime.getRuntime().exec(shell);
                process.getOutputStream().close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line = reader.readLine().trim();
                if(StringUtil.isNotBlank(line)){
                    osNumber = line;
                }

                reader.close();
                return osNumber;
            }
        } catch (IOException e) {
            log.error("Error Exception=", e);
        }
        return null;
    }

    /**
     * 修改系统时间
     */
    @Override
    public void syncTime(String time) {
        String osName = System.getProperty("os.name");
        String cmd = "";
        try {
            if (osName.matches("^(?i)Windows.*$")) {// Window 系统
                // 格式：yyyy-MM-dd
                cmd = " cmd /c date " + time.substring(0, 10);
                log.debug("cmd " + cmd);
                final Process processDate = Runtime.getRuntime().exec(cmd);
                printMessage(processDate.getInputStream());
                printMessage(processDate.getErrorStream());
                int value = processDate.waitFor();
                log.debug(value + "");

                // 格式 HH:mm:ss
                cmd = " cmd /c time " + time.substring(11, 19);
                log.debug("cmd " + cmd);
                final Process processTime = Runtime.getRuntime().exec(cmd);
                printMessage(processTime.getInputStream());
                printMessage(processTime.getErrorStream());
                value = processTime.waitFor();
                log.debug(value + "");
            } else {// Linux 系统
                // 格式：yyyyMMdd
                String dateStr = time.substring(0, 10).replace("-", "");
                cmd = " date -s " + dateStr;
                log.debug("cmd " + cmd);
                final Process processDate = Runtime.getRuntime().exec(cmd);
                printMessage(processDate.getInputStream());
                printMessage(processDate.getErrorStream());
                int value = processDate.waitFor();
                log.debug(value + "");

                // 格式 HH:mm:ss
                cmd = " date -s " + time.substring(11, 19);
                log.debug("cmd " + cmd);
                final Process processTime = Runtime.getRuntime().exec(cmd);
                printMessage(processTime.getInputStream());
                printMessage(processTime.getErrorStream());
                value = processTime.waitFor();
                log.debug(value + "");
            }
        } catch (IOException e) {
            log.error("Error Exception=", e);
        } catch (InterruptedException e) {
            log.error("Error Exception=", e);
        }
    }

    /**
     * 重启服务器
     */
    @Override
    public void rebootSystem() {
        String osName = System.getProperty("os.name");
        try {
            if (osName.matches("^(?i)Windows.*$")) {// Window 系统
                String cmd = "shutdown -r";
                final Process process = Runtime.getRuntime().exec(cmd);
                printMessage(process.getInputStream());
                printMessage(process.getErrorStream());
                int value = process.waitFor();
                log.debug(value + "");
            } else {// Linux系统
                String[] cmd = new String[] {"/bin/sh", "-c", "reboot"};
                final Process process = Runtime.getRuntime().exec(cmd);
                printMessage(process.getInputStream());
                printMessage(process.getErrorStream());
                int value = process.waitFor();
                log.debug(value + "");
            }
        } catch (IOException e) {
            log.error("Error Exception=", e);
        } catch (InterruptedException e) {
            log.error("Error Exception=", e);
        }
    }

    /**
     * 重启jar
     */
    @Override
    public BaseResult rebootTomcat() {
        String osName = System.getProperty("os.name");
        try {
            if (osName.matches("^(?i)Windows.*$")) {// Window 系统
                // 脚本方式重启
                String projectPath = System.getProperty("user.dir");
                String batPath = projectPath + "\\bin\\rebootTomcat.bat";
                String cmd = "cmd.exe /c " + "\"" + batPath + "\"";
                log.debug("cmd: " + cmd);
                final Process process = Runtime.getRuntime().exec(cmd);
                printMessage(process.getInputStream());
                printMessage(process.getErrorStream());
                int value = process.waitFor();
                if (value == 0) {
                    return new BaseResult();
                } else {
                    return new BaseResult("0", "重启失败", null);
                }

                // sc服务方式重启
//                String cmd = " cmd /c start sc stop Tomcat8";
//                final Process process = Runtime.getRuntime().exec(cmd);
//                printMessage(process.getInputStream());
//                printMessage(process.getErrorStream());
//                int value = process.waitFor();
//                log.debug(value + "");
//
//                // 这段代码无效，因为tomcat已经关闭，程序已停止
//                cmd = " cmd /c start sc start Tomcat8";
//                final Process process2 = Runtime.getRuntime().exec(cmd);
//                printMessage(process2.getInputStream());
//                printMessage(process2.getErrorStream());
//                value = process2.waitFor();
//                log.debug(value + "");
            } else {// Linux 系统
                String[] cmd = new String[] {"/bin/sh", "-c", "/usr/local/apache-tomcat-8.5.41/bin/shutdown.sh"};
                final Process process = Runtime.getRuntime().exec(cmd);
                printMessage(process.getInputStream());
                printMessage(process.getErrorStream());
                int value = process.waitFor();
                log.debug("cmd return " + value);
                if (value != 0) {
                    return new BaseResult("0", "重启失败", null);
                }

                cmd = new String[] {"/bin/sh", "-c", "/usr/local/apache-tomcat-8.5.41/bin/startup.sh"};
                final Process process2 = Runtime.getRuntime().exec(cmd);
                printMessage(process2.getInputStream());
                printMessage(process2.getErrorStream());
                value = process2.waitFor();
                log.debug("cmd return " + value);
                if (value == 0) {
                    return new BaseResult();
                } else {
                    return new BaseResult("0", "重启失败", null);
                }
            }
        } catch (IOException e) {
            log.error("Error Exception=", e);
        } catch (InterruptedException e) {
            log.error("Error Exception=", e);
        }
        return new BaseResult("0", "重启失败", null);
    }

    /**
     * 数据库备份
     */
    @Override
    public BaseResult exportDb(String savePath, String fileName) {
        Runtime rt = Runtime.getRuntime();
        Process pro;
        try {
            String[] cmd = getExportCmd(savePath, fileName);
            pro = rt.exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(pro.getErrorStream(), "GBK"));
            String errorLine = null;
            while ((errorLine = br.readLine()) != null) {
                // 1. 如果报错“'mysqldump' 不是内部或外部命令，也不是可运行的程序”，
                //    需要配置环境变量mysql路径，在环境变量path中加上"c:\program files\mysql\mysql server 5.6\bin"，
                //    修改后重启eclipse或tomcat才能生效
                // 2. 有的win10系统配置环境变量配置成功后，仍然从cmd窗口和java代码里面读取不到，此时要用绝对路径调用exe
                log.debug(errorLine);
            }
            br.close();
            int result = pro.waitFor();
            if (result == 0) {
                File dbFile = new File(savePath + "/" + fileName);
                return new BaseResult("1", "成功", (int) dbFile.length());
            } else {
                return new BaseResult("0", errorLine, null);
            }
        } catch (IOException e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", e.getMessage(), null);
        } catch (InterruptedException e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", e.getMessage(), null);
        }
    }

    /**
     * 数据库还原，注意必须使用服务器导出的sql，才能导入成功，不能使用自己用mysql软件导出的sql，
     * 因为自己导出的流程不全，里面没有执行use database等流程
     */
    @Override
    public BaseResult importDb(String savePath, String fileName) {
        Runtime rt = Runtime.getRuntime();
        Process pro;
        try {
            String[] cmd = getImportCmd(savePath, fileName);
            pro = rt.exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
            String errorLine = null;
            StringBuffer resultStr = new StringBuffer();
            while ((errorLine = br.readLine()) != null) {
                resultStr.append(errorLine);
                log.debug(errorLine);
            }
            br.close();
            int result = pro.waitFor();
            if (result == 0) {
                return new BaseResult();
            } else {
                return new BaseResult("0", resultStr.toString(), null);
            }
        } catch (IOException e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", e.getMessage(), null);
        } catch (InterruptedException e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", e.getMessage(), null);
        }
    }

    /**
     * 备份命令
     */
    String[] getExportCmd(String savePath, String fileName) {
        StringBuilder arg = new StringBuilder();
        String[] cmd = new String[3];
        String os = System.getProperties().getProperty("os.name");
        if (os.startsWith("Win")) {
            cmd[0] = "cmd.exe";
            cmd[1] = "/c";
            if (enviroment.equals("prod")) {// 发布模式，取mysql\bin绝对地址
                String projectPath = System.getProperty("user.dir");
                String mysqlPath = projectPath + "\\..\\mysql-5.6.44-winx64\\bin\\";
                log.debug("mysqlPath " + mysqlPath);
                arg.append("\"" + mysqlPath + "mysqldump" + "\"" + " ");
            } else {
                arg.append("mysqldump ");// 开发模式，已经手动设置环境变量
            }
        } else {
            cmd[0] = "/bin/sh";
            cmd[1] = "-c";
            arg.append("mysqldump ");
        }

        arg.append("-u" + dbUserName + " ");
        arg.append("-p" + dbPassword + " ");
        arg.append("--default-character-set=utf8 ");
        arg.append("--add-drop-table ");// 先drop再create
        arg.append("--routines ");// 转储数据库中的函数和程序
        arg.append("--triggers ");
        arg.append("--comments ");
        arg.append("--compress ");// 压缩传输
        arg.append("-r" + savePath + "/" + fileName + " ");
        arg.append("--databases " + dbName);
        cmd[2] = arg.toString();
        return cmd;
    }

    /**
     * 备份命令，有的win10系统设置了环境变量仍然读取不到，不能用这种方式
     */
    String[] getExportCmdUseless(String savePath, String fileName) {
        String[] cmd = new String[3];
        String os = System.getProperties().getProperty("os.name");
        if (os.startsWith("Win")) {
            cmd[0] = "cmd.exe";
            cmd[1] = "/c";
        } else {
            cmd[0] = "/bin/sh";
            cmd[1] = "-c";
        }

        StringBuilder arg = new StringBuilder();
        arg.append("mysqldump ");
        arg.append("-u" + dbUserName + " ");
        arg.append("-p" + dbPassword + " ");
        arg.append("--default-character-set=utf8 ");
        arg.append("--add-drop-table ");// 先drop再create
        arg.append("--routines ");// 转储数据库中的函数和程序
        arg.append("--triggers ");
        arg.append("--comments ");
        arg.append("--compress ");// 压缩传输
        arg.append("-r" + savePath + "/" + fileName + " ");
        arg.append("--databases " + dbName);
        cmd[2] = arg.toString();
        return cmd;
    }

    /**
     * 还原命令
     */
    String[] getImportCmd(String savePath, String fileName) {
        StringBuilder arg = new StringBuilder();
        String[] cmd = new String[3];
        String os = System.getProperties().getProperty("os.name");
        if (os.startsWith("Win")) {
            cmd[0] = "cmd.exe";
            cmd[1] = "/c";
        } else {
            cmd[0] = "/bin/sh";
            cmd[1] = "-c";
        }

        if (os.startsWith("Win")) {
            cmd[0] = "cmd.exe";
            cmd[1] = "/c";
            if (enviroment.equals("prod")) {// 发布模式，取mysql\bin绝对地址
                String projectPath = System.getProperty("user.dir");
                String mysqlPath = projectPath + "\\..\\mysql-5.6.44-winx64\\bin\\";
                log.debug("mysqlPath " + mysqlPath);
                arg.append("\"" + mysqlPath + "mysql" + "\"" + " ");
            } else {
                arg.append("mysql ");// 开发模式，已经手动设置环境变量
            }
        } else {
            cmd[0] = "/bin/sh";
            cmd[1] = "-c";
            arg.append("mysql ");
        }

        arg.append("-u" + dbUserName + " ");
        arg.append("-p" + dbPassword + " ");
        arg.append("< ");
        arg.append(savePath + "/" + fileName);
        cmd[2] = arg.toString();
        return cmd;
    }

    @Override
    public BaseResult getSystemInfo() {
        Map<String, Integer> map = new HashMap<>();
        SigarUtils.initSigar(sigarLibPath);
        try {
            map.put("cpu", cpuUsage());
            map.put("mem", memUsage());
            map.put("file", fileUsage());
        } catch (SigarException e) {
            log.error("Error Exception=", e);
        } catch (Exception e) {
            log.error("Error Exception=", e);
        }
        return new BaseResult(map);
    }

    int cpuUsage() throws SigarException {
        Sigar sigar = new Sigar();
        CpuPerc cpuList[] = sigar.getCpuPercList();

        // 取平均值
        double totalCpu = 0.0;
        for (int i = 0; i < cpuList.length; i++) {// 不管是单块CPU还是多CPU都适用
            totalCpu += cpuList[i].getCombined();
            //printCpuPerc(cpuList[i]);
        }
        double percent = (totalCpu * 100) / cpuList.length;

        // 取最大值
//        double percent = 0.0;
//        for (int i = 0; i < cpuList.length; i++) {
//            double value = cpuList[i].getCombined();
//            if (percent < value) {
//                percent = value;
//            }
//        }
//        percent = percent * 100;

        log.debug("cpu使用率 " + percent);
        return (int) percent;
    }

    static void printCpuPerc(CpuPerc cpu) {
        System.out.println("CPU用户使用率:    " + CpuPerc.format(cpu.getUser()));// 用户使用率
        System.out.println("CPU系统使用率:    " + CpuPerc.format(cpu.getSys()));// 系统使用率
        System.out.println("CPU当前等待率:    " + CpuPerc.format(cpu.getWait()));// 当前等待率
        System.out.println("CPU当前错误率:    " + CpuPerc.format(cpu.getNice()));//
        System.out.println("CPU当前空闲率:    " + CpuPerc.format(cpu.getIdle()));// 当前空闲率
        System.out.println("CPU总的使用率:    " + CpuPerc.format(cpu.getCombined()));// 总的使用率
    }

    int memUsage() throws SigarException {
        Sigar sigar = new Sigar();
        Mem mem = sigar.getMem();
        double percent = (mem.getUsed() * 100) / mem.getTotal();
        log.debug("内存使用率 " + percent);
        return (int) percent;
    }

    int fileUsage() throws Exception {
        Sigar sigar = new Sigar();
        FileSystem fslist[] = sigar.getFileSystemList();
        long totalNum = 0;
        long usedNum = 0;
        for (int i = 0; i < fslist.length; i++) {
            //System.out.println("分区的盘符名称" + i);
            FileSystem fs = fslist[i];
            FileSystemUsage usage = null;
            usage = sigar.getFileSystemUsage(fs.getDirName());
            switch (fs.getType()) {
            case 0: // TYPE_UNKNOWN ：未知
                break;
            case 1: // TYPE_NONE
                break;
            case 2: // TYPE_LOCAL_DISK : 本地硬盘
                totalNum += usage.getTotal();
                usedNum += usage.getUsed();
                break;
            case 3:// TYPE_NETWORK ：网络
                break;
            case 4:// TYPE_RAM_DISK ：闪存
                break;
            case 5:// TYPE_CDROM ：光驱
                break;
            case 6:// TYPE_SWAP ：页面交换
                break;
            }
        }
        double percent = 0;
        if (totalNum != 0) {
            percent = (usedNum * 100) / totalNum;
        }
        log.debug("硬盘使用率 " + percent);
        return (int) percent;
    }


    void printMessage(final InputStream input) {
        new Thread(new Runnable() {
            public void run() {
                Reader reader = null;
                try {
                    reader = new InputStreamReader(input, "GBK");
                } catch (UnsupportedEncodingException e1) {
                    log.error("Error Exception=", e1);
                    return;
                }
                BufferedReader bf = new BufferedReader(reader);
                String line = null;
                try {
                    while ((line = bf.readLine()) != null) {
                        log.debug("cmd readLine: " + line);
                    }
                } catch (IOException e) {
                    log.error("Error Exception=", e);
                }
            }
        }).start();
    }

}
