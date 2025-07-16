package cn.ushare.account.admin.schedule;

import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;


/**
 * 用户定时上报心跳包
 */
@Configuration
@EnableScheduling
@Slf4j
public class CompanyHeartBeat {
	 
	@Autowired
    SystemConfigService configService;
    @Autowired
    LicenceCache licenceCache;
	
	@Scheduled(cron = "${schedule.heartBeatUpdateTime}")
    public void scheduler() throws Exception {
        sendHeartBeat();
    }

    /**
     * 请发送心跳包
     */
    void sendHeartBeat() {
        // 获取请求地址
        String reqUrl = "http://www.ushareyun.net/system/auth/heartbeat.json";
        LicenceInfo licenceInfo = licenceCache.getLicenceInfo();
        String authPhone = null != licenceInfo ? licenceInfo.getAccount() : null;

        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            post = new HttpPost(reqUrl);
            post.setHeader("Accept", "application/json; charset=utf-8");
            // 构建消息实体
            List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
            pairList.add(new BasicNameValuePair("p", authPhone));
            post.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));

            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.info("请求出错: " + statusCode);
            } else {
                String resultStr = EntityUtils.toString(response.getEntity(), "utf-8");
                log.debug("heartbeat request resp " + resultStr);
                if (StringUtil.isBlank(resultStr)) {
                    log.info("心跳接口返回为空");
                } else {
                    JSONObject resultMap = JSONObject.fromObject(resultStr);
                    if (resultMap.get("code") == null) {
                        log.info("心跳接口返回参数错误");
                    }
                    int resultCode = resultMap.optInt("code");
                    if (resultCode != 100) {
                        if (resultMap.get("message") != null) {
                            log.info(resultMap.get("message").toString());
                        } else {
                            log.info("心跳接口失败");
                        }
                    } else {
                        if(null!=resultMap.get("data") && "0".equals(resultMap.get("data"))){
                            //用户被禁用
                            configService.updateByCode("AUTH_SERVER_VALID", "0");
                        }
                    }

                }
            }
        } catch (Exception e) {
            log.error("Error Exception=", e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }
}
