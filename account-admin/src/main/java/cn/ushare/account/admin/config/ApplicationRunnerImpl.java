package cn.ushare.account.admin.config;

import cn.ushare.account.admin.cache.MiniAccessToken;
import cn.ushare.account.admin.portal.service.PortalService;
import cn.ushare.account.admin.radius.service.RadiusAccountService;
import cn.ushare.account.admin.radius.service.RadiusService;
import cn.ushare.account.admin.service.WxConfigService;
import cn.ushare.account.admin.service.impl.LicenceLoaderService;
import cn.ushare.account.util.HttpClientUtil;
import lombok.Data;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * 工程启动完成后执行的程序
 */
@Component
@Slf4j
@Data
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Value("${enviroment}")
    private String enviroment;
    @Autowired
    WxConfigService wxConfigService;
    @Value("${weixin.mini.appid}")
    String appId;
    @Value("${weixin.mini.secret}")
    String appSeret;
    @Value("${weixin.mini.baseAccessTokenURL}")
    String baseAccessTokenURL;
//    @Value("${weixin.mini.redirectUrl}")
//    String redirectUrl;
//    @Value("${weixin.mini.selectOpenIdUrl}")
//    String selectOpenIdUrl;
//    @Value("${weixin.mini.selectAllOpenIdUrl}")
//    String selectAllOpenIdUrl;
//    @Value("weixin.mini.deleteOpenIdUrl")
//    String deleteOpenIdUrl;

    public static MiniAccessToken tokenObj = new MiniAccessToken();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("Package mode " + enviroment);
        new Thread(new RadiusService()).start();
        new Thread(new RadiusAccountService()).start();
        new Thread(new LicenceLoaderService()).start();
        new Thread(new PortalService()).start();

        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("grant_type","ushareyun_credential");

            String tokenResult = HttpClientUtil.doPost(baseAccessTokenURL, paramMap);
            log.info("tokenResult:===" + tokenResult);
            JSONObject tokenJson = JSONObject.fromObject(tokenResult);
            JSONObject data = tokenJson.getJSONObject("data");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            format.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            Date expiresTime = format.parse(data.optString("expireTime"));
            String accessToken = data.optString("accessToken", "");

            //fill data when system start
            tokenObj.setAccessToken(accessToken);
            tokenObj.setExpireTime(expiresTime);

        } catch (Exception ex){
            log.error("Error Exception=", ex);
        }
    }
}
