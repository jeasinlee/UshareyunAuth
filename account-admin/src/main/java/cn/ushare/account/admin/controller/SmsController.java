package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.SmsConfigService;
import cn.ushare.account.admin.service.SmsRecordService;
import cn.ushare.account.admin.service.SmsSendService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "SmsController", description = "短信接口")
@RestController
@Slf4j
@RequestMapping("/sms")
public class SmsController {

    private final static String moduleName = "短信接口";

    @Autowired
    HttpServletRequest request;
    @Autowired
    SessionService sessionService;
    @Autowired
    SmsRecordService smsRecordService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    SmsConfigService smsConfigService;
    @Autowired
    SmsSendService smsSendService;

    /**
     * 发送短信
     */
    @ApiOperation(value="发送短信", notes="")
    @SystemLogTag(description="发送短信", moduleName=moduleName)
    @RequestMapping(value="/send", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult send(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        String phone = (String) param.get("phone");
        Integer businessType = (Integer) param.get("businessType");
        String checkCode = (String) param.get("checkCode");// 图片验证码

        String showCode = systemConfigService.getByCode("VALID_CODE");
        if("1".equals(showCode)) {
            // 检查图片验证码
            if (request.getSession().getAttribute("checkCode") == null) {
                return new BaseResult("0", "验证码失效", null);
            }

            String rightCheckCode = (String) request.getSession()
                    .getAttribute("checkCode");
            if (StringUtils.isBlank(checkCode)
                    || StringUtils.isBlank(rightCheckCode)
                    || !checkCode.equals(rightCheckCode)) {
                request.getSession().removeAttribute("checkCode");
                return new BaseResult("0", "验证码错误", null);
            }
            request.getSession().removeAttribute("checkCode");
        }

        String smsCode = (int) ((Math.random()*9 + 1) * 1000) + "";
        return smsSendService.send(phone, smsCode, businessType);
    }

}
