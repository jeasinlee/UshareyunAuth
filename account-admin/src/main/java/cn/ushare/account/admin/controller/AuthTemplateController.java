package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AuthTemplateService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AuthTemplate;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
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
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AuthTemplateController", description = "认证模板")
@RestController
@Slf4j
@RequestMapping("/authTemplate")
public class AuthTemplateController {

    private final static String moduleName = "认证模板";

    @Autowired
    SessionService sessionService;
    @Autowired
    AuthTemplateService authTemplateService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AuthTemplate> add(@RequestBody @Valid String authTemplateJson) throws Exception {
        AuthTemplate authTemplate = JsonObjUtils.json2obj(authTemplateJson, AuthTemplate.class);
        authTemplate.setIsValid(1);
        authTemplate.setUpdateTime(new Date());
        authTemplateService.save(authTemplate);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AuthTemplate> update(@RequestBody String authTemplateJson) throws Exception {
        AuthTemplate authTemplate = JsonObjUtils.json2obj(authTemplateJson, AuthTemplate.class);
        authTemplateService.updateById(authTemplate);
        return new BaseResult("1", "成功", authTemplate);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        authTemplateService.removeById(id);
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
        List<Integer> authTemplateList = new ArrayList<>();
        for (Integer id : ids) {
            authTemplateList.add(id);
        }
        authTemplateService.removeByIds(authTemplateList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AuthTemplate> get(@RequestBody String id) {
        AuthTemplate authTemplate = authTemplateService.getInfo(Integer.parseInt(id.replace("\"", "")));
        return new BaseResult("1", "成功", authTemplate);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<AuthTemplate>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<AuthTemplate> page = new Page<AuthTemplate>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");

            if (queryParams.containsKey("name") && null != queryParams.get("name")) {
                wrapper.like("t.name", queryParams.get("name"));
            }
            if (queryParams.containsKey("companyName") && null != queryParams.get("companyName")) {
                wrapper.like("t.company_name", queryParams.get("companyName"));
            }
        }
        page = authTemplateService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
