package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.SmsConfigService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.SmsConfig;
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

import javax.validation.Valid;
import java.util.*;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "SmsConfigController", description = "短信配置")
@RestController
@Slf4j
@RequestMapping("/smsConfig")
public class SmsConfigController {

    private final static String moduleName = "短信配置";

    @Autowired
    SessionService sessionService;
    @Autowired
    SmsConfigService smsConfigService;
    @Autowired
    SystemConfigService systemConfigService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<SmsConfig> add(@RequestBody @Valid String smsConfigJson) throws Exception {
        SmsConfig smsConfig = JsonObjUtils.json2obj(smsConfigJson, SmsConfig.class);
        smsConfig.setIsValid(1);
        smsConfig.setUpdateTime(new Date());
        smsConfigService.save(smsConfig);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<SmsConfig> update(@RequestBody String smsConfigJson) throws Exception {
        SmsConfig smsConfig = JsonObjUtils.json2obj(smsConfigJson, SmsConfig.class);
        return smsConfigService.update(smsConfig);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        SmsConfig smsConfig = new SmsConfig();
        smsConfig.setId(id);
        smsConfig.setIsValid(0);
        smsConfigService.updateById(smsConfig);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<SmsConfig> smsConfigList = new ArrayList<>();
        for (Integer id : ids) {
            SmsConfig smsConfig = new SmsConfig();
            smsConfig.setId(id);
            smsConfig.setIsValid(0);
            smsConfigList.add(smsConfig);
        }
        smsConfigService.updateBatchById(smsConfigList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<SmsConfig> get(@RequestBody String id) {
        SmsConfig smsConfig = smsConfigService.getById(id);
        return new BaseResult("1", "成功", smsConfig);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="分页查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getList() throws Exception {
        // 列表
        List<SmsConfig> list = smsConfigService.list(new QueryWrapper());
        // 当前选用的短信配置ID
        String selectedConfigId = systemConfigService.getByCode("SMS-SERVER-ID");
        // 返回
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("selectedConfigId", selectedConfigId);
        resultMap.put("list", list);
        return new BaseResult(resultMap);
    }

}
