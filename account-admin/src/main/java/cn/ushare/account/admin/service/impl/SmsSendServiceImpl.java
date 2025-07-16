package cn.ushare.account.admin.service.impl;

import cn.hutool.http.HttpRequest;
import cn.ushare.account.admin.service.SmsConfigService;
import cn.ushare.account.admin.service.SmsRecordService;
import cn.ushare.account.admin.service.SmsSendService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.SmsConfig;
import cn.ushare.account.entity.SmsRecord;
import cn.ushare.account.util.HttpClientUtil;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.StringUtil;
import cn.ushare.account.util.XmlUtil;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class SmsSendServiceImpl implements SmsSendService {

    @Autowired
    SmsRecordService smsRecordService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    SmsConfigService smsConfigService;

    @Value("${excessiveSmsNum}")
    Integer excessiveSmsNum;

    @Value("${webservices.account}")
    String account;
    @Value("${webservices.password}")
    String password;

    /**
     * @param businessType 业务类型，1访客认证，2找回密码
     */
    @Override
    public BaseResult send(String phone, String text, Integer businessType) throws Exception {
        // 查询短信服务商
        String smsServerId = systemConfigService.getByCode("SMS-SERVER-ID");
        String smsLeftStr = systemConfigService.getByCode("SMS-LEFT");
        SmsConfig smsConfig = smsConfigService.getById(smsServerId);

        // 是否防恶意下发
        if (smsConfig.getIsExcessiveSendForbid() == 1) {
            // 统计该号码今天发送次数
            Integer todaySend = smsRecordService.countToday(phone);
            if (todaySend > excessiveSmsNum) {
                return new BaseResult("0", "该号码已超过当天发送上限", null);
            }
        }

        // 发送短信
        BaseResult sendResult = null;
        if (smsConfig.getId() == 1) {
            sendResult = aliSend(smsConfig.getAccessKey(), smsConfig.getAccessSecret(),
                    smsConfig.getSign(), smsConfig.getTemplateId(),
                    smsConfig.getMsgText(), phone, text);
        } else if (smsConfig.getId() == 2) {
            sendResult = tencentSend(Integer.valueOf(smsConfig.getAccessKey()),
                    smsConfig.getAccessSecret(),
                    Integer.valueOf(smsConfig.getTemplateId()),
                    smsConfig.getSign(), phone, text);
        } else if (smsConfig.getId() == 3) {
            sendResult = huyiSend(smsConfig, smsConfig.getTemplateId(), phone, text);
        } else {
            sendResult = zhutongSend(smsConfig, smsConfig.getTemplateId(), phone, text, "");
        }

        // 保存发送记录
        SmsRecord smsRecord = new SmsRecord();
        smsRecord.setBusinessType(businessType);// 1访客认证，2找回密码
        smsRecord.setSmsCompanyId(smsConfig.getId());
        smsRecord.setPhone(phone);
        smsRecord.setCheckCode(text);
        smsRecord.setIsValid(1);
        smsRecord.setUpdateTime(new Date());
        if (sendResult.getReturnCode().equals("1")) {
            smsRecord.setResult(1);

            //扣减剩余短信数量
            Integer leftCount = Integer.parseInt(smsLeftStr);
            if(leftCount <= 0){
                return new BaseResult("0", "短信库存不足，请充值", null);
            }
            leftCount -= 1;
            systemConfigService.updateByCode("SMS-LEFT", leftCount.toString());

        } else {
            smsRecord.setResult(0);
        }
        smsRecordService.save(smsRecord);
        return sendResult;
    }

    /**
     * 发送警报短信
     */
    @Override
    public BaseResult sendAlarm(String phone) throws Exception {
        // 查询短信服务商
        String smsServerId = systemConfigService.getByCode("SMS-SERVER-ID");
        SmsConfig smsConfig = smsConfigService.getById(smsServerId);

        // 发送短信
        BaseResult sendResult = null;
        if (smsConfig.getId() == 1) {
            sendResult = aliSend(smsConfig.getAccessKey(), smsConfig.getAccessSecret(),
                    smsConfig.getSign(), smsConfig.getAlarmTemplateId(),
                    smsConfig.getAlarmMsgText(), phone, null);
        } else if (smsConfig.getId() == 2) {
            sendResult = tencentSend(Integer.valueOf(smsConfig.getAccessKey()),
                    smsConfig.getAccessSecret(),
                    Integer.valueOf(smsConfig.getAlarmTemplateId()),
                    smsConfig.getSign(), phone, null);
        } else if (smsConfig.getId() == 3) {
            sendResult = huyiSend(smsConfig, smsConfig.getUrl(),
                    phone, "");
        } else {
            sendResult = zhutongSend(smsConfig, smsConfig.getAlarmTemplateId(), phone, "", "");
        }

        // 保存发送记录
        SmsRecord smsRecord = new SmsRecord();
        smsRecord.setBusinessType(3);// 1访客认证，2找回密码，3系统警报
        smsRecord.setSmsCompanyId(smsConfig.getId());
        smsRecord.setPhone(phone);
        smsRecord.setCheckCode(null);
        smsRecord.setIsValid(1);
        smsRecord.setUpdateTime(new Date());
        if (sendResult.getReturnCode().equals("1")) {
            smsRecord.setResult(1);
        } else {
            smsRecord.setResult(0);
        }
        smsRecordService.save(smsRecord);

        return new BaseResult();
    }

    @Override
    public BaseResult sendByWebService(String phone, String text) {
        String reqUrl = "http://172.16.152.140/SMSService/Service1.asmx";

        //拼接好xml
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "xmlns:tns=\"http://www.ideal.sh.cn/sms\" " +
                "xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n");
        sb.append("<soap:Body>\n");
        sb.append("<tns:SendSms>\n");
        sb.append("<tns:account>" + account + "</tns:account>\n");
        sb.append("<tns:password>" + password + "</tns:password>\n");
        sb.append("<tns:mobile>" + phone + "</tns:mobile>\n");
        sb.append("<tns:content>您登录网络的验证码是："+text+"。【运营中心WLAN】</tns:content>\n");
        sb.append("<tns:scheduleTime></tns:scheduleTime>\n");
        sb.append("</tns:SendSms>\n");
        sb.append("</soap:Body>\n");
        sb.append("</soap:Envelope>\n");
        String xmlStr = sb.toString();
        log.error("===request:" + xmlStr);

        String resultXml = HttpClientUtil.doPostWebservice(reqUrl, xmlStr);
        try {
            log.error("===repsonse:" + resultXml);

            // 保存发送记录
            SmsRecord smsRecord = new SmsRecord();
            smsRecord.setBusinessType(1);// 1短信认证，2找回密码，3系统警报
            smsRecord.setSmsCompanyId(-1);
            smsRecord.setPhone(phone);
            smsRecord.setCheckCode(text);
            smsRecord.setIsValid(1);
            smsRecord.setUpdateTime(new Date());
            smsRecord.setResult(1);

            smsRecordService.save(smsRecord);

            return new BaseResult();
        } catch (Exception e) {
            return new BaseResult("-1", "认证失败", null);
        }

    }

    /**
     * 阿里云短信发送
     */
    private BaseResult aliSend(String accessKey, String accessSecret,
            String sign, String templateId, String msgText,
            String phone, String code) throws Exception {
        DefaultProfile profile = DefaultProfile.getProfile("default",
                accessKey, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", sign);
        request.putQueryParameter("TemplateCode", templateId);
        // 短信模板中的${}字段
        String textModel = msgText;
        if (textModel.indexOf("{") >= 0) {// 有变量
            String key = textModel.substring(textModel.indexOf("{") + 1, textModel.indexOf("}"));
            String param = "{" + key + ":" + code + "}";
            //log.debug("param " + param);
            request.putQueryParameter("TemplateParam", param);
        } else {// 无变量，code随意
            request.putQueryParameter("TemplateParam", "{\"code\":\"1234\"}");
        }

        CommonResponse response = client.getCommonResponse(request);
        log.debug("sms response " + response.getData());
        Map<String, Object> responseData = (Map<String, Object>) JsonObjUtils.json2map(response.getData());
        if (((String) responseData.get("Code")).equals("OK")) {
            return new BaseResult();
        } else {
            return new BaseResult("0", (String) responseData.get("Message"), null);
        }
    }

    /**
     * 腾讯云短信发送
     */
    @SuppressWarnings("unchecked")
    private BaseResult tencentSend(Integer accessKey, String accessSecret,
            Integer templateId, String sign,
            String phone, String code) throws Exception {
        String[] params = null;
        if (code == null) {
            params = new String[]{};
        } else {
            params = new String[]{code};
        }
        SmsSingleSender ssender = new SmsSingleSender(accessKey, accessSecret);
        SmsSingleSenderResult result = ssender.sendWithParam("86", phone,
                templateId, params, sign, "", "");
        log.debug("tencent sms result " + result.toString());
        if (result.result == 0) {
            return new BaseResult();
        } else {
            return new BaseResult("0", result.errMsg, null);
        }
    }

    /**
     * 互亿无线短信发送
     */
    /**
     * 互亿无线短信发送
     */
    private BaseResult huyiSend(SmsConfig smsConfig, String msgText, String phone, String code) throws Exception {
        String result = null;
        String charset = "utf-8";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(smsConfig.getUrl());
        httpPost.addHeader("ContentType", "application/x-www-form-urlencoded;charset=GBK");
        //String content = new String("您的验证码是：" + code + "。请不要把验证码泄露给其他人。");
        String content = msgText;
        content = content.replace("【变量】", code);
        log.debug(content);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("account", smsConfig.getAccessKey()));
        list.add(new BasicNameValuePair("password", smsConfig.getAccessSecret()));
        list.add(new BasicNameValuePair("mobile", phone));
        list.add(new BasicNameValuePair("content", content));
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(list, charset);
        httpPost.setEntity(formEntity);

        CloseableHttpResponse response = httpClient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            log.error("短信接口状态码错误，请检查网络");
            return new BaseResult("0", "短信接口状态码错误，请检查网络", null);
        }

        HttpEntity entity = response.getEntity();
        String resultStr = EntityUtils.toString(entity, charset);
        log.info(resultStr);
        Map<String, String> resultMap = XmlUtil.xmlToMap(resultStr);
        if (!((String) resultMap.get("code")).equals("2")) {
            return new BaseResult("0", resultMap.get("msg"), null);
        }
        // 关闭
        response.close();
        httpPost.abort();
        httpClient.close();
        return new BaseResult();
    }

    /**
     * 助通科技短信发送
     */
    private BaseResult zhutongSend(SmsConfig smsConfig, String templateId,
                                   String phone, String code, String mobile) throws Exception {
        String tKey = System.currentTimeMillis() / 1000 + "";

        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("username", smsConfig.getAccessKey());
        jsonObject.put("password", StringUtil.signureText(smsConfig.getAccessSecret(), tKey));
        jsonObject.put("tKey", tKey);
        jsonObject.put("tpId", templateId);
        jsonObject.put("signature", smsConfig.getSign());

        com.alibaba.fastjson.JSONArray records = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONObject record = new com.alibaba.fastjson.JSONObject();
        record.put("mobile", phone);
        com.alibaba.fastjson.JSONObject param = new com.alibaba.fastjson.JSONObject();
        if(StringUtils.isNotBlank(code)) {
            param.put("valid_code", code);
        }

        record.put("tpContent", param);
        records.add(record);

        jsonObject.put("records", records);
        String json = JsonObjUtils.obj2json(jsonObject);
        log.debug(":zhutong===" + json);

        String result = HttpRequest.post(smsConfig.getUrl())
                .timeout(60000)
                .body(json, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .execute().body();

        log.info("助通发送结果：===" + result);
        com.alibaba.fastjson.JSONObject resultMap = com.alibaba.fastjson.JSONObject.parseObject(result);
        if (200 != resultMap.getIntValue("code")) {
            return new BaseResult("-1", "发送失败", null);
        }

        return new BaseResult();
    }

}
