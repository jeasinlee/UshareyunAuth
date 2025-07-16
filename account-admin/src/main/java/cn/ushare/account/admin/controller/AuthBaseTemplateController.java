package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AuthBaseTemplateService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AuthBaseTemplate;
import cn.ushare.account.entity.BaseResult;
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
 * @since 2019-03-27
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AuthBaseTemplateController", description = "认证基础模板")
@RestController
@Slf4j
@RequestMapping("/authBaseTemplate")
public class AuthBaseTemplateController {

    @Autowired
    SessionService sessionService;
    @Autowired
    AuthBaseTemplateService authBaseTemplateService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<AuthBaseTemplate> add(@RequestBody @Valid String authBaseTemplateJson) throws Exception {
        AuthBaseTemplate authBaseTemplate = JsonObjUtils.json2obj(authBaseTemplateJson, AuthBaseTemplate.class);
        authBaseTemplate.setIsValid(1);
        authBaseTemplate.setUpdateTime(new Date());
        authBaseTemplateService.save(authBaseTemplate);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<AuthBaseTemplate> update(@RequestBody String authBaseTemplateJson) throws Exception {
        AuthBaseTemplate authBaseTemplate = JsonObjUtils.json2obj(authBaseTemplateJson, AuthBaseTemplate.class);
        authBaseTemplateService.updateById(authBaseTemplate);
        return new BaseResult("1", "成功", authBaseTemplate);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        authBaseTemplateService.removeById(id);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<Integer> authBaseTemplateList = new ArrayList<>();
        for (Integer id : ids) {
            authBaseTemplateList.add(id);
        }
        authBaseTemplateService.removeByIds(authBaseTemplateList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<AuthBaseTemplate> get(@RequestBody String id) {
        AuthBaseTemplate authBaseTemplate = authBaseTemplateService.getById(id);
        return new BaseResult("1", "成功", authBaseTemplate);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<AuthBaseTemplate> page = new Page<AuthBaseTemplate>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }

        page = authBaseTemplateService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
