package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.admin.service.AuthMethodService;
import cn.ushare.account.admin.service.SsidService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.AuthMethod;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Ssid;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import cn.ushare.account.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AuthMethodController", description = "认证方式")
@RestController
@Slf4j
@RequestMapping("/authMethod")
public class AuthMethodController {

    private final static String moduleName = "认证方式";

    @Autowired
    SessionService sessionService;
    @Autowired
    AuthMethodService authMethodService;
    @Autowired
    AcService acService;
    @Autowired
    SsidService ssidService;

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<AuthMethod> update(@RequestBody String authMethodJson) throws Exception {
        AuthMethod authMethod = JsonObjUtils.json2obj(authMethodJson, AuthMethod.class);
        authMethod.setCustomPolicyId(null);// 策略ID固定，不允许修改
        authMethodService.updateById(authMethod);

        //去除控制器及SSID中启用的认证方式
        List<Ac> acList = acService.list(new QueryWrapper<>());
        if(CollectionUtils.isNotEmpty(acList)){
            for (Ac ac : acList){
                String methods = ac.getAuthMethod();
                if(methods.contains(authMethod.getId()+"")){
                    methods = StringUtil.removeOne(methods, authMethod.getId());
                    ac.setAuthMethod(methods);
                    acService.updateById(ac);
                }
            }
        }
        List<Ssid> ssidList = ssidService.list(new QueryWrapper<>());
        if(CollectionUtils.isNotEmpty(ssidList)){
            for (Ssid ssid : ssidList){
                String methods = ssid.getAuthMethod();
                if(methods.contains(authMethod.getId()+"")){
                    methods = StringUtil.removeOne(methods, authMethod.getId());
                    ssid.setAuthMethod(methods);
                    ssidService.updateById(ssid);
                }
            }
        }

        return new BaseResult("1", "成功", authMethod);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult getList(@RequestBody String paramJSON) throws Exception {
        Map param = JsonObjUtils.json2map(paramJSON);
        Page<AuthMethod> page = new Page<AuthMethod>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<?, ?>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("isEnable") && !"".equals(queryParams.get("isEnable").toString())) {
                wrapper.like("is_enable", queryParams.get("isEnable"));
            }
        }

        page = authMethodService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
