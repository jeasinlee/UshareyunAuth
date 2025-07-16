package cn.ushare.account.admin.pay.tenpay;

import cn.hutool.json.JSONUtil;
import cn.ushare.account.entity.AccountOrders;
import cn.ushare.account.util.HttpClientUtil;
import cn.ushare.account.util.IpUtil;
import cn.ushare.account.util.JsonObjUtils;
import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.core.enums.RequestMethod;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.enums.WxApiType;
import com.ijpay.wxpay.enums.WxDomain;
import com.ijpay.wxpay.model.v3.Amount;
import com.ijpay.wxpay.model.v3.H5Info;
import com.ijpay.wxpay.model.v3.SceneInfo;
import com.ijpay.wxpay.model.v3.UnifiedOrderModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class WeixinPayService {

    @Autowired
    HttpServletRequest request;
    @Autowired
    WXPayConfig wxPayConfig;
    @Value("${ushareyun.tenpay.notifyUrl}")
    private String notifyUrl;
    @Value("${ushareyun.server.orderQueryUrl}")
    private String orderQueryUrl;
    @Value("${ushareyun.server.createOrderUrl}")
    private String createOrderUrl;

    public String unifiedOrder(AccountOrders accountOrders, String deviceType) throws Exception{
        //h5下单
        String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
        SceneInfo sceneInfo = new SceneInfo();

        H5Info h5Info = new H5Info().setType(deviceType);
        sceneInfo.setPayer_client_ip(IpUtil.getIpAddr(request)).setH5_info(h5Info);

        UnifiedOrderModel unifiedOrderModel = new UnifiedOrderModel()
                .setAppid(wxPayConfig.getAppID())
                .setMchid(wxPayConfig.getMchID())
                .setDescription(accountOrders.getProductName())
                .setOut_trade_no(accountOrders.getOrderNum())
                .setTime_expire(timeExpire)
                .setNotify_url(notifyUrl)
                .setAmount(new Amount().setTotal(accountOrders.getTotalFee()).setCurrency("CNY"));
        if(!deviceType.equalsIgnoreCase("pc")) {
            unifiedOrderModel = unifiedOrderModel.setScene_info(sceneInfo);
        }

        log.info("deviceType:" + deviceType);
        log.info("统一下单参数 {}", JSONUtil.toJsonStr(unifiedOrderModel));
        IJPayHttpResponse response = WxPayApi.v3(
                RequestMethod.POST,
                WxDomain.CHINA.toString(),
                !deviceType.equalsIgnoreCase("pc") ?
                        WxApiType.H5_PAY.toString() : WxApiType.NATIVE_PAY.toString(),
                wxPayConfig.getMchID(),
                wxPayConfig.getSerialNumber(),
                null,
                wxPayConfig.getKeyPath(),
                JSONUtil.toJsonStr(unifiedOrderModel)
        );
        log.info("统一下单响应 {}", response);
        // 根据证书序列号查询对应的证书来验证签名结果
        boolean verifySignature = WxPayKit.verifySignature(response, wxPayConfig.getPlatformCertPath());
        log.info("verifySignature: {}", verifySignature);

        //服务器创建订单
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("order_num", accountOrders.getOrderNum());
        paramMap.put("merchant_id", wxPayConfig.getMchID());
        paramMap.put("total_fee", accountOrders.getTotalFee()+"");
        paramMap.put("pay_type", "1");

        String resultString = HttpClientUtil.doPostMap(createOrderUrl, paramMap);
        log.info("服务器创建微信订单响应: {}", resultString);

        return response.getBody();
    }

    public String queryOrder(AccountOrders accountOrders) throws Exception{
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("order_num", accountOrders.getOrderNum());
        paramMap.put("merchant_id", wxPayConfig.getMchID());

        String resultString = HttpClientUtil.doPostMap(orderQueryUrl, paramMap);
        log.info("服务器微信订单查询响应： {}", resultString);
        return resultString;
    }
}
