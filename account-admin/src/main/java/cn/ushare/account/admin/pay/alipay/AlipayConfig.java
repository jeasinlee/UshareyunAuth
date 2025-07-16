package cn.ushare.account.admin.pay.alipay;

import cn.ushare.account.admin.service.AccountPlatformConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("alipayConfig")
public class AlipayConfig {
    @Autowired
    AccountPlatformConfigService configService;

    @Value("${pay.alipay.appid}")
    String appid;
    @Value("${pay.alipay.privateKey}")
    String privateKey;
    @Value("${pay.alipay.publicKey}")
    String publicKey;
    @Value("${pay.alipay.appCertPath}")
    String appCertPath;
    @Value("${pay.alipay.alipayCertPath}")
    String alipayCertPath;
    @Value("${pay.alipay.alipayRootCertPath}")
    String alipayRootCertPath;
    @Value("${pay.alipay.alipayServerUrl}")
    String alipayServerUrl;

    public String  getAppid(){
        return appid;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getAppCertPath() {
        return appCertPath;
    }

    public String getAlipayRootCertPath() {
        return alipayRootCertPath;
    }

    public String getAlipayCertPath() {
        return alipayCertPath;
    }

    public String getAlipayServerUrl() {
        return alipayServerUrl;
    }
}
