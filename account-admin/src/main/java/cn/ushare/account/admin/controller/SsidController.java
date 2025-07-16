package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.admin.service.SsidService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Ssid;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "SsidController", description = "SSID")
@RestController
@Slf4j
@RequestMapping("/ssid")
public class SsidController {

    private final static String moduleName = "Ssid管理";

    @Autowired
    SessionService sessionService;
    @Autowired
    SsidService ssidService;

    @Autowired
    AcService acService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<Ssid> add(@RequestBody String ssid) throws Exception {
        return ssidService.add(JsonObjUtils.json2obj(ssid, Ssid.class));
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<Ssid> update(@RequestBody String ssid) throws Exception {
        return ssidService.update(JsonObjUtils.json2obj(ssid, Ssid.class));
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody String id) {
        ssidService.removeById(Integer.parseInt(id)); //物理删除热点记录
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
        List<Integer> ssidList = new ArrayList<>();
        for (Integer id : ids) {
            ssidList.add(id);
        }
        ssidService.removeByIds(ssidList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<Ssid> get(@RequestBody String id) {
        Ssid ssid = ssidService.getById(id);
        Ac ac = acService.getOne(new QueryWrapper<Ac>().eq("id", ssid.getAcId()), false);
        if(null!=ac) {
            if (null == ssid.getAuthTemplateId() || "".equals(ssid.getAuthTemplateId())) {
                ssid.setAuthTemplateId(ac.getAuthTemplateId());
            }
            if (null == ssid.getAuthMethod() || "".equals(ssid.getAuthMethod())) {
                ssid.setAuthMethod(ac.getAuthMethod());
            }
        }
        return new BaseResult("1", "成功", ssid);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="查询", moduleName=SsidController.moduleName)
    public BaseResult<BasePageResult<Ssid>> getList(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<Ssid> page = new Page<Ssid>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams = new HashMap<>();
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("isEmployee") && !"".equals(queryParams.get("isEmployee"))) {
                wrapper.eq("s.is_employee", queryParams.get("isEmployee"));
            }
            if (null != queryParams.get("name") && !"".equals(queryParams.get("name"))) {
                wrapper.eq("s.name", queryParams.get("name"));
            }
        }

        page = ssidService.getList(page, wrapper, queryParams);

        return new BaseResult(page);
    }

}
