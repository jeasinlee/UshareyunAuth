package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.admin.service.DeviceBrandService;
import cn.ushare.account.admin.service.LicenceService;
import cn.ushare.account.admin.service.SsidService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.*;
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
@Api(tags = "AcController", description = "控制器")
@RestController
@Slf4j
@RequestMapping("/ac")
public class AcController {

    private final static String moduleName = "控制器管理";

    @Autowired
    SessionService sessionService;
    @Autowired
    AcService acService;
    @Autowired
    SsidService ssidService;
    @Autowired
    DeviceBrandService brandService;
    @Autowired
    LicenceService licenceService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="新增", moduleName=moduleName)
    public BaseResult<Ac> add(@RequestBody @Valid Ac ac) throws Exception {
        // 检查Licence授权
        BaseResult result = licenceService.checkInfo();
        if (!result.getReturnCode().equals("1")) {
            return result;
        }

        // 查询ip是否重复
        QueryWrapper<Ac> queryWrapper = new QueryWrapper();
        queryWrapper.eq("ip", ac.getIp());
        queryWrapper.eq("is_valid", 1);
        List<Ac> list = acService.list(queryWrapper);
        if (list.size() > 0) {
            return new BaseResult("0", "IP地址重复", null);
        }

        // 查询名称是否重复
        QueryWrapper<Ac> nameQuery = new QueryWrapper();
        nameQuery.eq("name", ac.getName());
        nameQuery.eq("is_valid", 1);
        list = acService.list(nameQuery);
        if (list.size() > 0) {
            return new BaseResult("0", "设备名称重复", null);
        }

        DeviceBrand brand = brandService.getById(ac.getBrandId());
        if("wired".equals(brand.getCode())) {
            //有线ac只能允许一个
            QueryWrapper<Ac> limituery = new QueryWrapper();
            limituery.eq("is_valid", 1);
            limituery.eq("brand_id", ac.getBrandId());
            list = acService.list(limituery);
            if (list.size() > 0) {
                return new BaseResult("0", "有线AC已经存在一个了", null);
            }
            ac.setIsWired(1);
        }

        ac.setBrandCode(brand.getCode());
        ac.setIsValid(1);
        ac.setUpdateTime(new Date());
        acService.save(ac);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="修改", moduleName=AcController.moduleName)
    public BaseResult<Ac> update(@RequestBody Ac ac) throws Exception {
        // 查询ip是否重复
        QueryWrapper<Ac> queryWrapper = new QueryWrapper();
        queryWrapper.eq("ip", ac.getIp());
        queryWrapper.eq("is_valid", 1);
        List<Ac> list = acService.list(queryWrapper);
        if (list.size() > 0) {
            if (!list.get(0).getId().equals(ac.getId())) {
                return new BaseResult("0", "IP地址重复", null);
            }
        }

        // 查询名称是否重复
        QueryWrapper<Ac> nameQuery = new QueryWrapper();
        nameQuery.eq("name", ac.getName());
        nameQuery.eq("is_valid", 1);
        list = acService.list(nameQuery);
        if (list.size() > 0) {
            if (!list.get(0).getId().equals(ac.getId())) {
                return new BaseResult("0", "设备名称重复", null);
            }
        }

        DeviceBrand brand = brandService.getById(ac.getBrandId());
        if("wired".equals(brand.getCode())) {
            //有线ac只能允许一个
            QueryWrapper<Ac> limituery = new QueryWrapper();
            limituery.eq("is_valid", 1);
            limituery.eq("brand_id", ac.getBrandId());
            list = acService.list(limituery);
            if (list.size() > 0) {
                if (!list.get(0).getId().equals(ac.getId())) {
                    return new BaseResult("0", "有线AC最多只能允许一个", null);
                }
            }
            ac.setIsWired(1);
        }

        acService.updateById(ac);

        //重置SSID的认证方式
        List<Ssid> ssidList = ssidService.list(new QueryWrapper<Ssid>().eq("ac_id", ac.getId()));
        ssidList.parallelStream().forEach(s ->{
            s.setAuthMethod(ac.getAuthMethod());
            ssidService.updateById(s);
        });
        return new BaseResult("1", "成功", ac);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="删除", moduleName=AcController.moduleName)
    public BaseResult delete(@RequestBody Integer id) {
        Ac ac = new Ac();
        ac.setId(id);
        ac.setIsValid(0);
        acService.updateById(ac);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="批量删除", moduleName=AcController.moduleName)
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<Ac> acList = new ArrayList<>();
        for (Integer id : ids) {
            Ac ac = new Ac();
            ac.setId(id);
            ac.setIsValid(0);
            acList.add(ac);
        }
        acService.updateBatchById(acList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="查询详情", moduleName=AcController.moduleName)
    public BaseResult<Ac> get(@RequestBody String id) {
        Ac ac = acService.getById(id);
        return new BaseResult("1", "成功", ac);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="查询列表", moduleName=AcController.moduleName)
    public BaseResult<BasePageResult<Ac>> getList(@RequestBody Map<String, Object> param) throws Exception {
        Page<Ac> page = new Page<Ac>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<Ac> wrapper = new QueryWrapper<>();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("name") && !"".equals(queryParams.get("name").toString())) {
                wrapper.like("ac.name", queryParams.get("name"));
            }
            if (null != queryParams.get("ip") && !"".equals(queryParams.get("ip").toString())) {
                wrapper.and(q -> q.eq("ac.ip", queryParams.get("ip"))
                        .or().eq("ac.nas_ip", queryParams.get("ip")));
            }
        }

        wrapper.eq("ac.is_valid", 1);
        page = acService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
