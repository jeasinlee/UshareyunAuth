package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.DevicePortalVersionService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.DevicePortalVersion;
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
 * @date 2019-03-25
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "DevicePortalVersionController", description = "")
@RestController
@Slf4j
@RequestMapping("/devicePortalVersion")
public class DevicePortalVersionController {

    @Autowired
    SessionService sessionService;
    @Autowired
    DevicePortalVersionService devicePortalVersionService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DevicePortalVersion> add(@RequestBody @Valid String devicePortalVersionJson) throws Exception {
        DevicePortalVersion devicePortalVersion = JsonObjUtils.json2obj(devicePortalVersionJson, DevicePortalVersion.class);
        devicePortalVersion.setIsValid(1);
        devicePortalVersion.setUpdateTime(new Date());
        devicePortalVersionService.save(devicePortalVersion);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DevicePortalVersion> update(@RequestBody String devicePortalVersionJson) throws Exception {
        DevicePortalVersion devicePortalVersion = JsonObjUtils.json2obj(devicePortalVersionJson, DevicePortalVersion.class);
        devicePortalVersionService.updateById(devicePortalVersion);
        return new BaseResult("1", "成功", devicePortalVersion);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        DevicePortalVersion devicePortalVersion = new DevicePortalVersion();
        devicePortalVersion.setId(id);
        devicePortalVersion.setIsValid(0);
        devicePortalVersionService.updateById(devicePortalVersion);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<DevicePortalVersion> devicePortalVersionList = new ArrayList<>();
        for (Integer id : ids) {
            DevicePortalVersion devicePortalVersion = new DevicePortalVersion();
            devicePortalVersion.setId(id);
            devicePortalVersion.setIsValid(0);
            devicePortalVersionList.add(devicePortalVersion);
        }
        devicePortalVersionService.updateBatchById(devicePortalVersionList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DevicePortalVersion> get(@RequestBody String id) {
        DevicePortalVersion devicePortalVersion = devicePortalVersionService.getById(id);
        return new BaseResult("1", "成功", devicePortalVersion);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<DevicePortalVersion>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<DevicePortalVersion> page = new Page<DevicePortalVersion>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = devicePortalVersionService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
