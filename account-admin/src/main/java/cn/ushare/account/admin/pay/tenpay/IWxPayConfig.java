package cn.ushare.account.admin.pay.tenpay;

import cn.hutool.core.io.FileUtil;
import com.ijpay.core.kit.PayKit;
import com.ijpay.wxpay.enums.WxDomain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;

@Component
public class IWxPayConfig extends WXPayConfig {

    @Value("${pay.tenpay.appid}")
    String appid;
    @Value("${pay.tenpay.privateKey}")
    String privateKey;
    @Value("${pay.tenpay.merchantId}")
    String merchantId;
    @Value("${pay.tenpay.certPath}")
    String certPath;
    @Value("${pay.tenpay.keyPath}")
    String keyPath;
    @Value("${pay.tenpay.platformCertPath}")
    String platformCertPath;

    private byte[] certData;

    public IWxPayConfig() throws Exception { // 构造方法读取证书, 通过getCertStream 可以使sdk获取到证书
        String certPath = "classpath:ushareyun20220418_cert.p12";
        File file = ResourceUtils.getFile(certPath);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    @Override
    String getAppID() {
        return appid;
    }

    @Override
    String getMchID() {
        return merchantId;
    }

    @Override
    String getSerialNumber() {
        // 获取证书序列号
        X509Certificate certificate = PayKit.getCertificate(FileUtil.getInputStream(certPath));
        return certificate.getSerialNumber().toString(16).toUpperCase();
    }

    @Override
    String getKeyPath() {
        return keyPath;
    }

    @Override
    String getPlatformCertPath() {
        return platformCertPath;
    }

    @Override
    String getKey() {
        return privateKey;
    }

    @Override
    InputStream getCertStream() {
        return new ByteArrayInputStream(this.certData);
    }

    @Override
    public IWXPayDomain getWXPayDomain() { // 这个方法需要这样实现, 否则无法正常初始化WXPay
        IWXPayDomain iwxPayDomain = new IWXPayDomain() {
            @Override
            public void report(String domain, long elapsedTimeMillis, Exception ex) {

            }

            @Override
            public DomainInfo getDomain(WXPayConfig config) {
                return new IWXPayDomain.DomainInfo(WxDomain.CHINA.getType(), true);
            }
        };
        return iwxPayDomain;
    }
}
