package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.portal.service.PortalUtil;
import cn.ushare.account.admin.service.MuteDeviceService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.MuteDevice;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.MacUtil;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

/**
 * @author jixiang.li
 * @since 2021-12-20
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "MuteDeviceController", description = "哑终端管理")
@RestController
@Slf4j
@RequestMapping("/muteDevice")
public class MuteDeviceController {

    private final static String moduleName = "哑终端列表";

    @Autowired
    MuteDeviceService deviceService;

    @ApiOperation(value="新增", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<MuteDevice> add(@RequestBody @Valid String apJson) throws Exception {
        MuteDevice muteDevice = JsonObjUtils.json2obj(apJson, MuteDevice.class);
        muteDevice.setIsValid(1);
        muteDevice.setUpdateTime(new Date());
        if(StringUtils.isNotBlank(muteDevice.getBindMac())){
            muteDevice.setBindMac(PortalUtil.MacFormat1(muteDevice.getBindMac()));
        }
        deviceService.save(muteDevice);
        return new BaseResult();
    }

    @ApiOperation(value="修改", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<MuteDevice> update(@RequestBody String apJson) throws Exception {
        MuteDevice muteDevice = JsonObjUtils.json2obj(apJson, MuteDevice.class);
        if(StringUtils.isNotBlank(muteDevice.getBindMac())){
            muteDevice.setBindMac(PortalUtil.MacFormat1(muteDevice.getBindMac()));
        }
        deviceService.updateById(muteDevice);
        return new BaseResult("1", "成功", muteDevice);
    }

    @ApiOperation(value="删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        deviceService.removeById(id);
        return new BaseResult();
    }

    @ApiOperation(value="批量删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<Integer> apList = new ArrayList<>();
        for (Integer id : ids) {
            apList.add(id);
        }
        deviceService.removeByIds(apList);
        return new BaseResult();
    }

    @ApiOperation(value="查询详情", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<MuteDevice> get(@RequestBody String id) {
        MuteDevice muteDevice = deviceService.getById(id);
        if(StringUtils.isNotBlank(muteDevice.getBindMac())){
            muteDevice.setBindMac(PortalUtil.MacFormat(muteDevice.getBindMac()));
        }
        return new BaseResult("1", "成功", muteDevice);
    }

    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询详情", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult<BasePageResult<MuteDevice>> getList(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<MuteDevice> page = new Page<MuteDevice>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("bindMac") && !"".equals(queryParams.get("bindMac").toString())) {
                wrapper.like("bind_mac", queryParams.get("bindMac"));
            }
            if (null != queryParams.get("bindIp") && !"".equals(queryParams.get("bindIp").toString())) {
                wrapper.like("bind_ip", queryParams.get("bindIp"));
            }
            if (null != queryParams.get("bindPurpose") && !"".equals(queryParams.get("bindPurpose").toString())) {
                wrapper.like("bind_purpose", queryParams.get("bindPurpose"));
            }
        }
        page = deviceService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
