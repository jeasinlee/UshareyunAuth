package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.DeviceSoftwareVersionService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.DeviceSoftwareVersion;
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
@Api(tags = "DeviceSoftwareVersionController", description = "设备软件版本")
@RestController
@Slf4j
@RequestMapping("/deviceSoftwareVersion")
public class DeviceSoftwareVersionController {

    @Autowired
    SessionService sessionService;
    @Autowired
    DeviceSoftwareVersionService deviceSoftwareVersionService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DeviceSoftwareVersion> add(@RequestBody @Valid String deviceSoftwareVersionJson) throws Exception {
        DeviceSoftwareVersion deviceSoftwareVersion = JsonObjUtils.json2obj(deviceSoftwareVersionJson, DeviceSoftwareVersion.class);
        deviceSoftwareVersion.setIsValid(1);
        deviceSoftwareVersion.setUpdateTime(new Date());
        deviceSoftwareVersionService.save(deviceSoftwareVersion);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DeviceSoftwareVersion> update(@RequestBody String deviceSoftwareVersionJson) throws Exception {
        DeviceSoftwareVersion deviceSoftwareVersion = JsonObjUtils.json2obj(deviceSoftwareVersionJson, DeviceSoftwareVersion.class);
        deviceSoftwareVersionService.updateById(deviceSoftwareVersion);
        return new BaseResult("1", "成功", deviceSoftwareVersion);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        DeviceSoftwareVersion deviceSoftwareVersion = new DeviceSoftwareVersion();
        deviceSoftwareVersion.setId(id);
        deviceSoftwareVersion.setIsValid(0);
        deviceSoftwareVersionService.updateById(deviceSoftwareVersion);
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
        List<DeviceSoftwareVersion> deviceSoftwareVersionList = new ArrayList<>();
        for (Integer id : ids) {
            DeviceSoftwareVersion deviceSoftwareVersion = new DeviceSoftwareVersion();
            deviceSoftwareVersion.setId(id);
            deviceSoftwareVersion.setIsValid(0);
            deviceSoftwareVersionList.add(deviceSoftwareVersion);
        }
        deviceSoftwareVersionService.updateBatchById(deviceSoftwareVersionList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DeviceSoftwareVersion> get(@RequestBody String id) {
        DeviceSoftwareVersion deviceSoftwareVersion = deviceSoftwareVersionService.getById(id);
        return new BaseResult("1", "成功", deviceSoftwareVersion);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<DeviceSoftwareVersion>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<DeviceSoftwareVersion> page = new Page<DeviceSoftwareVersion>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }

        page = deviceSoftwareVersionService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
