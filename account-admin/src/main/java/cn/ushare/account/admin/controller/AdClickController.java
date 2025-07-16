package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AdClickService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AdClick;
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
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AdClickController", description = "广告点击")
@RestController
@Slf4j
@RequestMapping("/adClick")
public class AdClickController {

    private final static String moduleName = "广告点击";

    @Autowired
    SessionService sessionService;
    @Autowired
    AdClickService adClickService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="新增", moduleName=moduleName)
    public BaseResult<AdClick> add(@RequestBody @Valid String adClickJson) throws Exception {
        AdClick adClick = JsonObjUtils.json2obj(adClickJson, AdClick.class);
        adClick.setIsValid(1);
        adClick.setUpdateTime(new Date());
        adClickService.save(adClick);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="修改", moduleName=moduleName)
    public BaseResult<AdClick> update(@RequestBody String adClickJson) throws Exception {
        AdClick adClick = JsonObjUtils.json2obj(adClickJson, AdClick.class);
        adClickService.updateById(adClick);
        return new BaseResult("1", "成功", adClick);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="删除", moduleName=moduleName)
    public BaseResult delete(@RequestBody Long id) {
        AdClick adClick = new AdClick();
        adClick.setId(id);
        adClick.setIsValid(0);
        adClickService.updateById(adClick);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    public BaseResult batchDelete(@RequestBody Long[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<AdClick> adClickList = new ArrayList<>();
        for (Long id : ids) {
            AdClick adClick = new AdClick();
            adClick.setId(id);
            adClick.setIsValid(0);
            adClickList.add(adClick);
        }
        adClickService.updateBatchById(adClickList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<AdClick> get(@RequestBody String id) {
        AdClick adClick = adClickService.getById(id);
        return new BaseResult("1", "成功", adClick);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<AdClick> page = new Page<AdClick>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<AdClick> wrapper = new QueryWrapper<>();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = adClickService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
