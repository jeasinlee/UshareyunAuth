package cn.ushare.account.admin.schedule;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.ushare.account.admin.config.ApplicationRunnerImpl;
import cn.ushare.account.admin.mapper.WhiteListMapper;
import cn.ushare.account.admin.portal.service.*;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.dto.OpenIdResponse;
import cn.ushare.account.dto.ZIpOpenId;
import cn.ushare.account.entity.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import cn.ushare.account.admin.config.GlobalCache;
import cn.ushare.account.admin.config.GlobalCache.CacheObj;
import cn.ushare.account.dto.AuthLogoutParam;
import lombok.extern.slf4j.Slf4j;


/**
 * 临时上网用户检查，用于微信认证前的临时上网放行，超过1分钟没完成微信登录，强制下线
 */
@Configuration
@EnableScheduling
@Slf4j
public class TempLoginCheck {

	@Autowired
    HuaweiPortalService huaweiPortalService;
	@Autowired
    RuckusPortalService ruckusPortalService;
	@Autowired
    TplinkPortalService tplinkPortalService;
    @Autowired
    ArubaPortalService arubaPortalService;
	@Autowired
    H3cPortalService h3cPortalService;
	@Autowired
    RuijiePortalService ruijiePortalService;
	@Autowired
    AuthUserService authUserService;
	@Autowired
    GlobalCache globalCache;
	@Autowired
    AcService acService;
    @Autowired
    WxConfigService wxConfigService;
    @Autowired
    WhiteListMapper whiteListMapper;

    @Autowired
    AuthParamService authParamService;
    @Autowired
    AuthRecordService authRecordService;
    @Value("${weixin.mini.redirectUrl}")
    String redirectUrl;
    @Value("${weixin.mini.selectOpenIdUrl}")
    String selectOpenIdUrl;
    @Value("${weixin.mini.selectAllOpenIdUrl}")
    String selectAllOpenIdUrl;
    @Value("${weixin.mini.deleteOpenIdUrl}")
    String deleteOpenIdUrl;


    @Value("${tempAccess}")
    String tempAccess;

	private static boolean inProcess = false;

	@Scheduled(cron = "${schedule.tempLoginCheckTime}")
    public void scheduler() throws Exception {
	    if (inProcess) {
	        return;
	    }
	    inProcess = true;

	    try {

            /**
             * 查看公众号是否登入
             */

            covertMap();

    	    // 查询临时登录列表
    	    ConcurrentHashMap<String, CacheObj> tempLoginMap =
    	            globalCache.getTempLoginMap();

    	    for(Map.Entry<String, CacheObj> entry : tempLoginMap.entrySet()) {

                //删除白名单
                AuthRecord authRecord = authRecordService.getTopOne(entry.getKey());
                QueryWrapper<WhiteList> whiteListQueryWrapper = new QueryWrapper<>();
                whiteListQueryWrapper.eq("value", authRecord.getMac());
                whiteListQueryWrapper.eq("type", 2);
                int whiteLists = whiteListMapper.delete(whiteListQueryWrapper);
                log.error("删除成功whiteList"+whiteLists);




                CacheObj cacheObj = entry.getValue();
                Long createTime = (Long) cacheObj.getCreateTime();
                AuthParam authParam = (AuthParam) cacheObj.getCacheValue();
                log.debug("check tempLogin userIp " + authParam.getUserIp() + " createTime " + createTime / 1000);
                // 超过60秒，强制下线

                if (System.currentTimeMillis() - createTime > Integer.parseInt(tempAccess) * 60 * 1000) {
                    log.debug("kick tempLogin userIp " + authParam.getUserIp());
                    // 下线参数
                    AuthLogoutParam logoutParam = new AuthLogoutParam();
                    logoutParam.setUserIp(authParam.getUserIp());
                    logoutParam.setUserMac(authParam.getUserMac());
                    logoutParam.setAcIp(authParam.getAcIp());

                    // 查询ac设备品牌，调用不同设备接口
                    BaseResult acResult = acService.getInfoByAcIp(authParam.getAcIp());
                    if (acResult.getReturnCode().equals("1")) {
                        Ac ac = (Ac) acResult.getData();
                        String brandCode = ac.getBrand().getCode();
                        BaseResult result = new BaseResult();
                        log.debug("钉钉登录失败，自动下线 " + logoutParam.toString());
                        if (brandCode.contains("huawei")) {// 华为设备
                            result = huaweiPortalService.logout(logoutParam);
                            if (result.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                                result = huaweiPortalService.logoutByRadius(logoutParam);
                            }
                        } else if (brandCode.contains("ruijie")) {// 锐捷设备
                            result = ruijiePortalService.logout(logoutParam);
                            if (result.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                                result = ruijiePortalService.logoutByRadius(logoutParam);
                            }
                        } else if (brandCode.contains("h3c")) {// 新华三设备
                            result = h3cPortalService.logout(logoutParam);
                            if (result.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                                result = h3cPortalService.logoutByRadius(logoutParam);
                            }
                        } else if (brandCode.contains("ruckus")) {// ruckus设备，只支持coa下线
                            result = ruckusPortalService.logoutByRadius(logoutParam);
                        } else if (brandCode.contains("tplink")) {// tplink设备，只支持coa下线
                            result = tplinkPortalService.logoutByRadius(logoutParam);
                        } else if (brandCode.contains("aruba")) {// aruba设备，只支持coa下线
                            result = arubaPortalService.logoutByRadius(logoutParam);
                        }

                        // 更新在线记录状态
                        if (result.getReturnCode().equals("1")) {
                            authUserService.updateOfflineState(logoutParam.getUserMac());
                        }
                    }
                    globalCache.removeTempLogin(authParam.getUserIp());
                }
            }
	    } catch (Exception e) {
	        log.error("Error Exception=", e);
	        inProcess = false;
        }
	    inProcess = false;
    }

    private void covertMap(){
        ConcurrentHashMap<String, CacheObj> map =
                globalCache.getTempLoginMap();

        if (map.size()<=0){
            return;
        }
        /**
         * 用appId查询
         */
        List<ZIpOpenId> zIpOpenIds = new ArrayList<>();
        Page<WxConfig> page = new Page<WxConfig>(1, 10);
        Page<WxConfig> list = wxConfigService.getList(page, new QueryWrapper());
        WxConfig wxConfig = list.getRecords().get(0);
        String appId = wxConfig.getAppId();
        String url = selectAllOpenIdUrl +"?appId=" +appId;
        ObjectMapper objectMapper = new ObjectMapper();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            HttpResponse response = httpClient.execute(request);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            String result = EntityUtils.toString(response.getEntity());
            System.out.println(result);
            OpenIdResponse openIdResponse = objectMapper.readValue(result, OpenIdResponse.class);
            if (ObjectUtils.isNotEmpty(openIdResponse)){
                zIpOpenIds = openIdResponse.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * 查询出来后做处理如果ip相同就删除tempMap里内容
         */
        if (zIpOpenIds.size()>0){
            //如果map里包含就删除
            for (ZIpOpenId zIpOpenId : zIpOpenIds) {


                String ipAndOpenId = zIpOpenId.getIp();
                String[] wxes = ipAndOpenId.split("wx");
                String ip = wxes[0];

                if (map.containsKey(ip)){
                    /**
                     * 登入
                     */
                    wxLogin(ip,zIpOpenId.getOpenId());
                    /**
                     * 删除uShareYun的openId
                     */
                    deleteOpenId(ipAndOpenId);
                }

            }
        }
    }

    private void wxLogin(String userIp,String openId){
        AuthParam authParam = authParamService.getByUserIp(userIp);
        // 微信登录成功，保存wxOpenId
        if (authParam != null) {
            AuthRecord authRecord = authRecordService.getTopOne(userIp);
            authRecord.setAuthMethod(authParam.getAuthMethod());
            authRecord.setWxOpenId(openId);
//            authRecord.setWxTid(tid);
            authRecordService.updateById(authRecord);

            AuthUser authUser = new AuthUser();
            authUser.setMac(authRecord.getMac());
            authUser.setWxOpenId(openId);
//            authUser.setWxTid(tid);
            authUser.setAuthMethod(authParam.getAuthMethod());
            authUserService.updateByMac(authUser);
            // 删除临时登录记录
            globalCache.removeTempLogin(userIp);


        }
    }
    private void deleteOpenId(String ip){
        String url = deleteOpenIdUrl+"?ip="+ip;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            HttpResponse response = httpClient.execute(request);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            String result = EntityUtils.toString(response.getEntity());
            System.out.println(result);
            log.error("删除成功whiteList"+result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
