package cn.ushare.account.admin.portal.service;

import cn.ushare.account.admin.config.ApplicationContextProvider;
import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.entity.Ac;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author jixiang.lee
 * @Description
 * @Date create in 11:50 2020/1/7
 * @Modified BY
 */
@Slf4j
public class PortalService implements Runnable {

    private Integer listenPort = 50100;// 监听端口，固定值
    // private String updateUrl = null;// 推送地址
    private boolean isRunning = false;
    private DatagramSocket socket = null;

    AcService acService;

    //多线池
    private static ThreadPoolExecutor threadPool;
    private static int poolThread = 10;//开启1个线程(获得CPU核数，多少核就多少线程，最多不建议超过cpu的4倍 )/

    public static ThreadPoolExecutor getThreadPool() {
        if (threadPool == null) {
            //ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue workQueue,  RejectedExecutionHandler handler)
            /*corePoolSize： 线程池维护线程的最少数量
            maximumPoolSize：线程池维护线程的最大数量
            keepAliveTime： 线程池维护线程所允许的空闲时间
            unit： 线程池维护线程所允许的空闲时间的单位
            workQueue： 线程池所使用的缓冲队列
            handler： 线程池对拒绝任务的处理策略  */
            //创建一个3个线程的线程池,超时时间500秒
            threadPool = new ThreadPoolExecutor(2, poolThread, 500L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }
        return threadPool;
    }

    public PortalService() {
        this.acService = ApplicationContextProvider.getBean(AcService.class);
    }

    @Override
    protected void finalize() {
        isRunning = false;
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            log.error(e.getStackTrace().toString());
        }
    }

    @Override
    public void run() {
        log.debug("PortalService run");
        try {
            socket = new DatagramSocket(listenPort);
            isRunning = true;
            while (isRunning) {
                try {
                    byte[] buf = new byte[100];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);// 接收数据

                    Thread tempThread = new Thread(() -> handlerData(packet));
                    getThreadPool().submit(tempThread);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    log.error(e1.getStackTrace().toString());
                }
            }
        } catch (SocketException e) {
            log.error("Error Exception=", e);
        }
    }

    private void handlerData(DatagramPacket data) {
        try{
            String ip = data.getAddress().getHostAddress();
            int port = data.getPort();
            byte[] reqData = new byte[data.getLength()];
            for(int i=0;i<reqData.length;i++){
                reqData[i] = data.getData()[i];
            }
            log.info("Receive BAS ip:" + ip + ", Port:" + port + ", Packet Size:" + data.getLength() + ", Packet Text:" +
                    PortalUtil.Getbyte2HexString(reqData));

            Ac ac = acService.getOne(new QueryWrapper<Ac>().eq("ip", ip));
            if(null!=ac){
                String sharedSecret = ac.getShareKey();
                byte[] outData = buildOutData(sharedSecret, reqData);

                try {
                    DatagramPacket outPacket = new DatagramPacket(outData,
                            outData.length, InetAddress.getByName(ip), ac.getPort());
                    socket.send(outPacket);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                } finally {

                }
            }
        }catch (Exception ex){
            log.error(ex.getMessage());
        }

    }

    private byte[] buildOutData(String sharedSecret, byte[] reqData) {
        byte[] respData = null;
        // 构建portal协议中的字段包
        byte[] Ver = new byte[1];
        byte[] Type = new byte[1];
        byte[] Mod = new byte[1];
        byte[] Rsvd = new byte[1];
        byte[] SerialNo = new byte[2];
        byte[] ReqID = new byte[2];
        byte[] UserIP = new byte[4];
        byte[] UserPort = new byte[2];
        byte[] ErrCode = new byte[1];
        byte[] AttrNum = new byte[1];

        // 给各字段包赋初始值为接收到的包的值
        Ver[0] = reqData[0];
        Type[0] = reqData[1];
        Mod[0] = reqData[2];
        Rsvd[0] = reqData[3];
        SerialNo[0] = reqData[4];
        SerialNo[1] = reqData[5];
        ReqID[0] = reqData[6];
        ReqID[1] = reqData[7];
        UserIP[0] = reqData[8];
        UserIP[1] = reqData[9];
        UserIP[2] = reqData[10];
        UserIP[3] = reqData[11];
        UserPort[0] = reqData[12];
        UserPort[1] = reqData[13];
        ErrCode[0] = reqData[14];
        AttrNum[0] = reqData[15];

        // 主动下线报文
        if ((Type[0] & 0xFF) == 8) {

            if (((Ver[0] & 0xFF)) == 1) {
                respData = new byte[16];
            }
            if (((Ver[0] & 0xFF)) == 2) {
                respData = new byte[32];
            }
            for (int i = 0; i < 16; i++) {
                respData[i] = reqData[i];
            }

            short type = 14;
            respData[1] = (byte) type;
            respData[15] = (byte) 0;

            if (((Ver[0] & 0xFF)) == 2) {
                byte[] Attrs = new byte[0];
                byte[] BBuff = new byte[16];
                byte[] reqAuthen = new byte[16];
                for (int i = 0; i < 16; i++) {
                    BBuff[i] = respData[i];
                }
                if(reqData.length>=32){
                    for (int i = 0; i < 16; i++) {
                        reqAuthen[i] = reqData[16+i];
                    }
                }
                byte[] Authen = PortalUtil.makeAckAuthen(BBuff, Attrs, sharedSecret, reqAuthen);
                for (int i = 0; i < 16; i++) {
                    respData[16 + i] = Authen[i];
                }
            }
        }

        return respData;
    }
}
