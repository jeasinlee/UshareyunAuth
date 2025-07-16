package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.pay.alipay.AlipayService;
import cn.ushare.account.admin.pay.tenpay.WeixinPayService;
import cn.ushare.account.admin.service.AccountChargePolicyService;
import cn.ushare.account.admin.service.AccountChargeRecordService;
import cn.ushare.account.admin.service.AccountOrdersService;
import cn.ushare.account.admin.service.AccountUserService;
import cn.ushare.account.entity.*;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jixiang.li
 * @date 2022-04-05
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "PayController", description = "PayController")
@RestController
@Slf4j
@RequestMapping("/pay")
public class PayController {

    private final static String moduleName = "支付";
    @Autowired
    WeixinPayService weixinPayService;
    @Autowired
    AlipayService alipayService;
    @Autowired
    AccountChargePolicyService policyService;
    @Autowired
    AccountOrdersService ordersService;
    @Autowired
    AccountUserService userService;

    @Autowired
    AccountChargeRecordService chargeRecordService;
    @Autowired
    private HttpServletRequest request;
    Pattern pattern = Pattern.compile("weixin://[^s]*([\\w- ./?%&=]*)?");


    @ApiOperation(value = "创建订单", notes = "{'pid':12,'toUser':abc, 'fromUser':ABC, 'payType':0/1}")
    @RequestMapping(value = "/create_order", method = {RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description = "创建订单", moduleName = moduleName)
    public BaseResult createTenOrder(@RequestBody String jsonStr) throws Exception {
        JSONObject paramObj = JSONObject.fromObject(jsonStr);
        String pid = paramObj.optString("pid", "").trim();
        String fromUser = paramObj.optString("fromUser", null);
        String toUser = paramObj.optString("toUser", null);
        int payType = paramObj.optInt("payType", 0);
        if (StringUtils.isBlank(pid)) {
            return new BaseResult("-1", "套餐ID不能为空", "");
        }
        AccountUser accountToUser = userService.getDetail(toUser, 1);
        if (null == accountToUser) {
            return new BaseResult("-1", "受让方不存在", "");
        }
        AccountUser accountFromUser = userService.getDetail(fromUser, 1);
        if (null == accountFromUser) {
            return new BaseResult("-1", "充值方不存在", "");
        }
        AccountChargePolicy chargePolicy = policyService.getById(pid);
        if (null == chargePolicy) {
            return new BaseResult("-1", "套餐不存在", "");
        }

        String userAgent = request.getHeader("User-Agent");
        String deviceType = BrowseTypeUtil.getTerminalTypeStr(userAgent);

        AccountOrders accountOrder = new AccountOrders();
        accountOrder.setFromLoginName(fromUser);
        accountOrder.setFromUserId(accountFromUser.getId());
        accountOrder.setToLoginName(toUser);
        accountOrder.setToUserId(accountToUser.getId());
        accountOrder.setOrderNum(GzhUtil.generateOrderNum());
        accountOrder.setPolicyId(chargePolicy.getId());
        accountOrder.setProductName(chargePolicy.getPolicyName());
        accountOrder.setTotalFee(chargePolicy.getTotalFee());
        accountOrder.setPayType(payType);
        accountOrder.setOrderStatus(0);
        accountOrder.setChargeStatus(0);
        ordersService.saveObj(accountOrder);
        if(payType == 0){
            String resultString = alipayService.unifiedOrder(accountOrder, deviceType);
            Map<String, Object> resultMap = StringUtil.objectToMap(resultString);
            resultMap.put("orderNum", accountOrder.getOrderNum());

            return new BaseResult(resultMap);
        } else {
            String resultString = weixinPayService.unifiedOrder(accountOrder, deviceType);
            Map<String, Object> resultMap = StringUtil.objectToMap(resultString);
            resultMap.put("orderNum", accountOrder.getOrderNum());

            return new BaseResult(resultMap);
        }
    }

    @ApiOperation(value = "服务器调用", notes = "{'url':url}")
    @RequestMapping(value = "/ten_h5", method = {RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description = "服务器调用", moduleName = moduleName)
    public BaseResult createTenH5Order(@RequestBody String jsonStr) throws Exception {
        JSONObject paramObj = JSONObject.fromObject(jsonStr);
        String url = paramObj.optString("url", "").trim();

        String resultStr = HttpClientUtil.doGet(url);
        Matcher matcher = pattern.matcher(resultStr);
        String parseUrl = "";
        while (matcher.find()) {
            parseUrl = matcher.group();
            log.info(":==" + parseUrl);
        }

        return new BaseResult(parseUrl);
    }

    //查询订单
    @ApiOperation(value = "查询订单", notes = "{'orderNum':12}")
    @RequestMapping(value = "/query_order", method = {RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description = "查询订单", moduleName = moduleName)
    public BaseResult queryOrder(@RequestBody String jsonStr) throws Exception {
        BaseResult resultObj;
        JSONObject paramObj = JSONObject.fromObject(jsonStr);
        String orderNum = paramObj.optString("orderNum", "").trim();

        AccountOrders orders = ordersService.getByOrderNum(orderNum);
        if (null == orders) {
            resultObj = new BaseResult("-1", "订单号错误或订单不存在", null);
            return resultObj;
        }
        if (orders.getOrderStatus() == 1) {
            if (orders.getChargeStatus() == 0) {
                //给用户充值
                return chargeUser(orders, null);
            }
        } else {
            if (orders.getPayType() == 0) {
                //alipay
                String resultStr = alipayService.queryOrder(orders);
                JSONObject objectMap = JSONObject.fromObject(resultStr);
                if (objectMap.containsKey("returnCode") &&
                        objectMap.get("returnCode").toString().equalsIgnoreCase("1")) {
                    JSONObject data = objectMap.getJSONObject("data");
                    int status = data.optInt("status", 0);
                    if(1==status) {
                        return chargeUser(orders, data.optString("platform_order_num"));
                    } else {
                        return new BaseResult("-1", null, null);
                    }
                }
            } else {
                //tenpay
                String resultStr = weixinPayService.queryOrder(orders);
                JSONObject objectMap = JSONObject.fromObject(resultStr);
                if (objectMap.containsKey("returnCode") &&
                        objectMap.get("returnCode").toString().equalsIgnoreCase("1")) {
                    JSONObject data = objectMap.getJSONObject("data");
                    int status = data.optInt("status", 0);
                    if(1==status) {
                        return chargeUser(orders, data.optString("platform_order_num"));
                    } else {
                        return new BaseResult("-1", null, null);
                    }
                }
            }
        }
        return new BaseResult();
    }

    private BaseResult chargeUser(AccountOrders orders, String platformOrderNum) {
        AccountChargeRecord chargeRecord = chargeRecordService.getByOrderNum(orders.getOrderNum());
        if (null == chargeRecord) {
            chargeRecord = new AccountChargeRecord();
            chargeRecord.setLoginName(orders.getToLoginName());
            chargeRecord.setOrderId(orders.getId());
            chargeRecord.setOrderNum(orders.getOrderNum());
            chargeRecordService.addOrUpdate(chargeRecord);

            AccountChargePolicy chargePolicy = policyService.getById(orders.getPolicyId());
            AccountUser accountUser = userService.getById(orders.getToUserId());
            if (null != chargePolicy && null != accountUser) {
                DateTime expireTime = new DateTime(accountUser.getExpireTime());
                switch (chargePolicy.getUnit()) {
                    case 0:
                        expireTime = expireTime.plusDays(chargePolicy.getTotalNum());
                        break;
                    case 1:
                        expireTime = expireTime.plusMonths(chargePolicy.getTotalNum());
                        break;
                    case 2:
                        expireTime = expireTime.plusYears(chargePolicy.getTotalNum());
                        break;
                    case 3:
                        expireTime = expireTime.plusHours(chargePolicy.getTotalNum());
                        break;
                }

                accountUser.setChargePolicyId(chargePolicy.getId());
                accountUser.setIsDebt(0);
                accountUser.setIsLocked(0);
                accountUser.setExpireTime(expireTime.toDate());
                userService.updateById(accountUser);

                Date optTime = new Date();
                //更新订单状态
                orders.setOrderStatus(1);
                orders.setChargeStatus(1);
                if(StringUtils.isNotBlank(platformOrderNum)){
                    orders.setPlatformOrderNum(platformOrderNum);
                }
                orders.setChargeTime(optTime);
                if (orders.getOrderStatus() == 0) {
                    orders.setPayTime(optTime);
                }
                orders.setChargeTime(optTime);
                ordersService.updateById(orders);
            } else {
                return new BaseResult("-1", "订单号： " + orders.getOrderNum() + "充值记录异常", null);
            }
        }
        BaseResult resultObj = new BaseResult();
        resultObj.setReturnMsg("订单号： " + orders.getOrderNum() + "充值成功");
        return resultObj;
    }
}
