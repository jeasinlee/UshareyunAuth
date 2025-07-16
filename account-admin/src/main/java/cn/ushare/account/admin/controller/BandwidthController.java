package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.BandwidthService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.Bandwidth;
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
@Api(tags = "BandwidthController", description = "带宽管理")
@RestController
@Slf4j
@RequestMapping("/bandwidth")
public class BandwidthController {

    private final static String moduleName = "带宽管理";

    @Autowired
    SessionService sessionService;
    @Autowired
    BandwidthService bandwidthService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<Bandwidth> add(@RequestBody @Valid String bandwidthJson) throws Exception {
        Bandwidth bandwidth = JsonObjUtils.json2obj(bandwidthJson, Bandwidth.class);
        // 名称不能重复
        QueryWrapper<Bandwidth> repeatQuery = new QueryWrapper();
        repeatQuery.eq("name", bandwidth.getName());
        repeatQuery.eq("is_valid", 1);
        Bandwidth repeatOne = bandwidthService.getOne(repeatQuery);
        if (repeatOne != null) {
            return new BaseResult("0", "已有该名称的带宽", null);
        }

        bandwidth.setIsValid(1);
        bandwidth.setUpdateTime(new Date());
        bandwidthService.save(bandwidth);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<Bandwidth> update(@RequestBody String bandwidthJson) throws Exception {
        Bandwidth bandwidth = JsonObjUtils.json2obj(bandwidthJson, Bandwidth.class);
        // 名称不能重复
        QueryWrapper<Bandwidth> repeatQuery = new QueryWrapper();
        repeatQuery.eq("name", bandwidth.getName());
        repeatQuery.eq("is_valid", 1);
        Bandwidth repeatOne = bandwidthService.getOne(repeatQuery);
        if (repeatOne != null && repeatOne.getId() != bandwidth.getId()) {
            return new BaseResult("0", "已有该名称的带宽", null);
        }

        bandwidthService.updateById(bandwidth);
        return new BaseResult("1", "成功", bandwidth);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        bandwidthService.removeById(id);
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
        List<Integer> integerList = new ArrayList<>();
        for (Integer id : ids) {
            integerList.add(id);
        }
        bandwidthService.removeByIds(integerList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<Bandwidth> get(@RequestBody String id) {
        Bandwidth bandwidth = bandwidthService.getById(id);
        return new BaseResult("1", "成功", bandwidth);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<Bandwidth> page = new Page<Bandwidth>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (queryParams.containsKey("bandName") && null != queryParams.get("bandName")) {
                wrapper.like("name", queryParams.get("bandName"));
            }
        }
        page = bandwidthService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
