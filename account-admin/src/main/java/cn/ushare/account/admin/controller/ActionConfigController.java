package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.ActionConfigService;
import cn.ushare.account.admin.service.LicenceService;
import cn.ushare.account.entity.ActionConfig;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author jixiang.lee
 * @Description
 * @Date create in 11:25 2020/1/16
 * @Modified BY
 */
@Api(tags = "ActionConfigController", description = "行为管理配置")
@RestController
@Slf4j
@RequestMapping("/actionConfig")
public class ActionConfigController {

    private final static String moduleName = "行为管理配置";
    @Autowired
    ActionConfigService actionConfigService;
    @Autowired
    LicenceService licenceService;

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<ActionConfig> get(@RequestBody String id) {
        ActionConfig smsConfig = actionConfigService.getById(id);
        return new BaseResult("1", "成功", smsConfig);
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<ActionConfig> update(@RequestBody String actionConfigJson) throws Exception {
        ActionConfig actionConfig = JsonObjUtils.json2obj(actionConfigJson, ActionConfig.class);
        BaseResult licenceResult = licenceService.checkActionConfig();
        if (!licenceResult.getReturnCode().equals("1")) {
            new BaseResult<>("-1", "请升级授权", null);
        }

        return actionConfigService.update(actionConfig);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="分页查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult getList() throws Exception {
        // 列表
        List<ActionConfig> list = actionConfigService.list(new QueryWrapper());
        // 返回
        Map<String, Object> resultMap = new HashMap<>();
        ActionConfig actionConfig = list.stream().filter(config -> config.getIsCur() == 1).collect(Collectors.toList()).get(0);
        resultMap.put("list", list);
        resultMap.put("selectedConfigId", actionConfig.getId());
        return new BaseResult(resultMap);
    }

    /**
     * 检查行为管理授权
     */
    @ApiOperation(value="检查行为管理授权", notes="")
    @SystemLogTag(description="检查行为管理授权", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/checkAction", method={RequestMethod.POST})
    public BaseResult checkAction() throws Exception{
        // 检查Licence授权
        BaseResult licenceResult = licenceService.checkActionConfig();
        if (!licenceResult.getReturnCode().equals("1")) {
            return licenceResult;
        }

        return new BaseResult();
    }
}
