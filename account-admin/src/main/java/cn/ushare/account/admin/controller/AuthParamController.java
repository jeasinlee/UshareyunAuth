package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AuthParamService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AuthParam;
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
 * @since 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AuthParamController", description = "")
@RestController
@Slf4j
@RequestMapping("/authParam")
public class AuthParamController {

    @Autowired
    SessionService sessionService;
    @Autowired
    AuthParamService authParamService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<AuthParam> add(@RequestBody @Valid String authParamJson) throws Exception {
        AuthParam authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        authParam.setIsValid(1);
        authParam.setUpdateTime(new Date());
        authParamService.save(authParam);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<AuthParam> update(@RequestBody String authParamJson) throws Exception {
        AuthParam authParam = JsonObjUtils.json2obj(authParamJson, AuthParam.class);
        authParamService.updateById(authParam);
        return new BaseResult("1", "成功", authParam);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        AuthParam authParam = new AuthParam();
        authParam.setId(id);
        authParam.setIsValid(0);
        authParamService.updateById(authParam);
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
        List<AuthParam> authParamList = new ArrayList<>();
        for (Integer id : ids) {
            AuthParam authParam = new AuthParam();
            authParam.setId(id);
            authParam.setIsValid(0);
            authParamList.add(authParam);
        }
        authParamService.updateBatchById(authParamList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<AuthParam> get(@RequestBody String id) {
        AuthParam authParam = authParamService.getById(id);
        return new BaseResult("1", "成功", authParam);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<AuthParam> page = new Page<AuthParam>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = authParamService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
