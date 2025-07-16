package cn.ushare.account.admin.pay.alipay;

import cn.ushare.account.admin.service.AccountPlatformConfigService;
import cn.ushare.account.entity.AccountOrders;
import cn.ushare.account.util.HttpClientUtil;
import cn.ushare.account.util.JsonObjUtils;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class AlipayService {
    @Autowired
    HttpServletRequest request;

    @Value("${ushareyun.alipay.notifyUrl}")
    private String notifyUrl;
    @Value("${ushareyun.server.orderQueryUrl}")
    private String orderQueryUrl;
    @Value("${ushareyun.server.createOrderUrl}")
    private String createOrderUrl;

    @Autowired
    AccountPlatformConfigService configService;
    @Autowired
    AlipayConfig alipayConfig;

    Config config;

    private Config getConfig() {
        config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi.alipay.com";
        config.signType = "RSA2";
        config.appId = alipayConfig.appid;

        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        config.merchantPrivateKey = alipayConfig.privateKey;

        //注：证书文件路径支持设置为文件系统中的路径或CLASS_PATH中的路径，优先从文件系统中加载，加载失败后会继续尝试从CLASS_PATH中加载
        config.merchantCertPath = alipayConfig.appCertPath;
        config.alipayCertPath = alipayConfig.alipayCertPath;
        config.alipayRootCertPath = alipayConfig.alipayRootCertPath;

        //注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
        // config.alipayPublicKey = "<-- 请填写您的支付宝公钥，例如：MIIBIjANBg... -->";

        //可设置异步通知接收服务地址（可选）
        config.notifyUrl = notifyUrl;

        //可设置AES密钥，调用AES加解密相关接口时需要（可选）
//        config.encryptKey = "<-- 请填写您的AES密钥，例如：aa4BtZ4tspm2wnXLb1ThQA== -->";
        return config;
    }

    public String unifiedOrder(AccountOrders accountOrders, String deviceType) throws Exception{
        String responseString = "{}";
        Factory.setOptions(getConfig());
        if(!deviceType.equalsIgnoreCase("pc")) {
            AlipayTradeWapPayResponse payResponse = Factory.Payment.Wap().pay(accountOrders.getProductName(),
                    accountOrders.getOrderNum(),
                    new BigDecimal(accountOrders.getTotalFee()).divide(new BigDecimal(100)) + "",
                    null, null);
            responseString = payResponse.getBody();
        } else {
            AlipayTradePagePayResponse payResponse = Factory.Payment.Page().pay(accountOrders.getProductName(),
                    accountOrders.getOrderNum(),
                    new BigDecimal(accountOrders.getTotalFee()).divide(new BigDecimal(100)) + "",
                    null);
            responseString = payResponse.getBody();
        }

        //服务器创建订单
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("order_num", accountOrders.getOrderNum());
        paramMap.put("merchant_id", "100000");
        paramMap.put("total_fee", accountOrders.getTotalFee()+"");
        paramMap.put("pay_type", "0");

        String resultString = HttpClientUtil.doPostMap(createOrderUrl, paramMap);
        log.info("服务器创建支付宝订单响应: {}", resultString);

        return responseString;
    }

    public String queryOrder(AccountOrders accountOrders) throws Exception{
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("order_num", accountOrders.getOrderNum());
        paramMap.put("merchant_id", "100000");

        String resultString = HttpClientUtil.doPostMap(orderQueryUrl, paramMap);
        log.info("服务器支付宝订单查询响应： {}", resultString);
        return resultString;
    }
}
