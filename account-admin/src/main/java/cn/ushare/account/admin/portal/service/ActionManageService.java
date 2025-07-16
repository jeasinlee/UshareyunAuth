package cn.ushare.account.admin.portal.service;

import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.admin.service.ActionConfigService;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.ActionConfig;
import cn.ushare.account.entity.AuthParam;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author jixiang.lee
 * @Description
 * @Date create in 18:29 2020/1/14
 * @Modified BY
 */
@Service
@Slf4j
public class ActionManageService {

    @Autowired
    ActionConfigService actionConfigService;
    @Autowired
    AcService acService;

    /**
     * 单点登录接口
     *
     * @param authParam
     */
    public void login(AuthParam authParam) {
        //查询AC是否开启行为管理
        BaseResult<Ac> acInfo = acService.getInfoByAcIp(authParam.getAcIp());
        if(acInfo.data.getActionManage()==0){
            log.info("ac 未开启行为管理");
            return;
        }

        ActionConfig defActionConfig = actionConfigService.getOne(new QueryWrapper<ActionConfig>().
                eq("is_cur", 1));
        try {
            if (null != defActionConfig) {
                if ("sangfor".equalsIgnoreCase(defActionConfig.getMerchantCode())) {
                    //深信服网关
                    String url = "http://" + defActionConfig.getActionIp() + ":" + defActionConfig.getPort() + "/cgi-bin/caauth.cgi";
                    String params = "ui=web&opr=logon&chk_cookie=0&info=";
                    String paramSub = authParam.getUserIp() + "/" + authParam.getUserName() + "//";
                    paramSub = new String(paramSub.getBytes("utf-8"), "utf-8");
                    paramSub = StringUtil.encryptBASE64(paramSub);
                    paramSub = URLEncoder.encode(paramSub, "UTF-8");
                    params = params + paramSub;

                    sendGet(url, params);
                } else if ("pronetway".equalsIgnoreCase(defActionConfig.getMerchantCode())) {
                    //新网程
                    String url = "http://" + defActionConfig.getActionIp() + ":" + defActionConfig.getPort() + "/pronline/Msg";
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date now = new Date();
                    String timeS = format.format(now);
                    long timeLong = now.getTime() + 1000 * 60 * 60 * 24;
                    Date expiretime = new Date(timeLong);
                    String expiretimeS = format.format(expiretime);
                    String apMac = authParam.getApMac();
                    if (StringUtils.isBlank(authParam.getApMac())) {
                        apMac = authParam.getAcIp();
                    }
                    String params = "FunName@ncHttpLogin&account=" + authParam.getUserName() + "&expiretime=" + expiretimeS + "&ip=" + authParam.getUserIp() + "&mac=" + authParam.getUserMac() + "&time=" + timeS + "&location=" + apMac + "&netid=" + authParam.getAcIp();
                    sendGet(url, params);
                }
            }
        } catch (Exception ex) {
            log.error("==============ERROR Start=============");
            log.error(ex.getMessage());
            log.error("ERROR INFO ", ex.getStackTrace());
            log.error("==============ERROR End=============");
        }
    }

    /**
     * 单点登出接口
     *
     * @param authParam， port: 深信服85（默认）
     */
    public void logout(AuthParam authParam) {
        //查询AC是否开启行为管理
        BaseResult<Ac> acInfo = acService.getInfoByAcIp(authParam.getAcIp());
        if(acInfo.data.getActionManage()==0){
            log.info("ac 未开启行为管理");
            return;
        }

        ActionConfig defActionConfig = actionConfigService.getOne(new QueryWrapper<ActionConfig>().
                eq("is_cur", 1));
        try {
            if (null != defActionConfig) {
                if ("sangfor".equalsIgnoreCase(defActionConfig.getMerchantCode())) {
                    //深信服网关
                    log.error("Sangfor logout : serverIP=" + defActionConfig.getActionIp() + " userIP=" + authParam.getUserIp() + " userName=" + authParam.getUserName());
                    String url = "http://" + defActionConfig.getActionIp() + ":" + defActionConfig.getPort() + "/cgi-bin/caauth.cgi";
                    String params = "ui=web&opr=logout&chk_cookie=0&info=";
                    String paramSub = authParam.getUserIp() + "/" + authParam.getUserName() + "//";
                    paramSub = new String(paramSub.getBytes("utf-8"), "utf-8");
                    paramSub = StringUtil.encryptBASE64(paramSub);
                    paramSub = URLEncoder.encode(paramSub, "UTF-8");
                    params = params + paramSub;

                    sendGet(url, params);
                } else if ("pronetway".equalsIgnoreCase(defActionConfig.getMerchantCode())) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    String url = "http://" + defActionConfig.getActionIp() + ":" + defActionConfig.getPort() + "/pronline/Msg";
                    Date now = new Date();
                    String timeS = format.format(now);
                    String params = "FunName@ncHttpLogout&ip=" + authParam.getUserIp() + "&time=" + timeS;
                    sendGet(url, params);
                }
            }

        } catch (Exception e) {
            log.error("==============ERROR Start=============");
            log.error(e.getMessage());
            log.error("ERROR INFO ", e);
            log.error("==============ERROR End=============");
        }
    }

    /***
     *  向指定URL发送GET请求
     */
    public void sendGet(String url, String param) {
        log.info("====auth action:" + param);
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            log.info("====action resp: " + result);
        } catch (Exception e) {
            log.error("==============ERROR Start=============");
            log.error(e.getMessage());
            log.error("ERROR INFO ", e);
            log.error("==============ERROR End=============");
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {

            }
        }
    }
}
