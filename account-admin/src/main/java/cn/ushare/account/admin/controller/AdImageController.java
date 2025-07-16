package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AdImageService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AdImage;
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
@Api(tags = "AdImageController", description = "广告图片")
@RestController
@Slf4j
@RequestMapping("/adImage")
public class AdImageController {

    private final static String moduleName = "广告图片";

    @Autowired
    SessionService sessionService;
    @Autowired
    AdImageService adImageService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="新增", moduleName=moduleName)
    public BaseResult<AdImage> add(@RequestBody @Valid String adImageJson) throws Exception {
        AdImage adImage = JsonObjUtils.json2obj(adImageJson, AdImage.class);
        adImage.setIsValid(1);
        adImage.setUpdateTime(new Date());
        adImageService.save(adImage);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="修改", moduleName=moduleName)
    public BaseResult<AdImage> update(@RequestBody String adImageJson) throws Exception {
        AdImage adImage = JsonObjUtils.json2obj(adImageJson, AdImage.class);
        adImageService.updateById(adImage);
        return new BaseResult("1", "成功", adImage);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="删除", moduleName=moduleName)
    public BaseResult delete(@RequestBody Integer id) {
        adImageService.removeById(id);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }

        adImageService.removeByIds(Arrays.asList(ids));
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="查询", moduleName=moduleName)
    public BaseResult<AdImage> get(@RequestBody String id) {
        AdImage adImage = adImageService.getById(id);
        return new BaseResult("1", "成功", adImage);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<AdImage> page = new Page<AdImage>(1, 1000);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<AdImage> wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (queryParams.containsKey("advName") && null != queryParams.get("advName")) {
                wrapper.like("name", queryParams.get("advName"));
            }
        }
        page = adImageService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
