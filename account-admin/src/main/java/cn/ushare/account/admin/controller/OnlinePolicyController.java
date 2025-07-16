package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.OnlinePolicyService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.OnlinePolicy;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * @since 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "OnlinePolicyController", description = "策略配置")
@RestController
@Slf4j
@RequestMapping("/onlinePolicy")
public class OnlinePolicyController {

    private final static String moduleName = "策略配置";

    @Autowired
    SessionService sessionService;
    @Autowired
    OnlinePolicyService onlinePolicyService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<OnlinePolicy> add(@RequestBody @Valid String onlinePolicyJson) throws Exception {
        OnlinePolicy onlinePolicy = JsonObjUtils.json2obj(onlinePolicyJson, OnlinePolicy.class);
        onlinePolicy.setIsValid(1);
        onlinePolicy.setUpdateTime(new Date());
        onlinePolicyService.save(onlinePolicy);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<OnlinePolicy> update(@RequestBody String onlinePolicyJson) throws Exception {
        OnlinePolicy onlinePolicy = JsonObjUtils.json2obj(onlinePolicyJson, OnlinePolicy.class);
        onlinePolicyService.updateById(onlinePolicy);
        return new BaseResult("1", "成功", onlinePolicy);
    }

    /**
     * 修改默认配置
     */
    @ApiOperation(value="修改全局配置", notes="")
    @SystemLogTag(description="修改全局配置", moduleName=moduleName)
    @RequestMapping(value="/updateDefault", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<OnlinePolicy> updateDefault(@RequestBody String onlinePolicyJson) throws Exception {
        OnlinePolicy onlinePolicy = JsonObjUtils.json2obj(onlinePolicyJson, OnlinePolicy.class);
        if (onlinePolicy.getId() != 100) {
            return new BaseResult("0", "id错误", "null");
        }
        onlinePolicyService.updateById(onlinePolicy);
        return new BaseResult("1", "成功", onlinePolicy);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        OnlinePolicy onlinePolicy = new OnlinePolicy();
        onlinePolicy.setId(id);
        onlinePolicy.setIsValid(0);
        onlinePolicyService.updateById(onlinePolicy);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<OnlinePolicy> onlinePolicyList = new ArrayList<>();
        for (Integer id : ids) {
            OnlinePolicy onlinePolicy = new OnlinePolicy();
            onlinePolicy.setId(id);
            onlinePolicy.setIsValid(0);
            onlinePolicyList.add(onlinePolicy);
        }
        onlinePolicyService.updateBatchById(onlinePolicyList);
        return new BaseResult();
    }

    /**
     * 查询默认配置
     */
    @ApiOperation(value="查询全局配置", notes="")
    @SystemLogTag(description="查询全局配置", moduleName=moduleName)
    @RequestMapping(value="/getDefault", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<OnlinePolicy> getDefault() {
        // id为100的记录为系统默认配置
        OnlinePolicy onlinePolicy = onlinePolicyService.getById(100);
        return new BaseResult("1", "成功", onlinePolicy);
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<OnlinePolicy> get(@RequestBody String id) {
        OnlinePolicy onlinePolicy = onlinePolicyService.getById(id);
        return new BaseResult("1", "成功", onlinePolicy);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<OnlinePolicy>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<OnlinePolicy> page = new Page<OnlinePolicy>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = onlinePolicyService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
