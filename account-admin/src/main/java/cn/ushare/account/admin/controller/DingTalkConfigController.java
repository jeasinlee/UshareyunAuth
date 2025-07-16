package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.admin.service.DingTalkConfigService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.DingTalkConfig;
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
 * @date 2019-07-29
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "DingTalkConfigController", description = "")
@RestController
@Slf4j
@RequestMapping("/dingTalkConfig")
public class DingTalkConfigController {

    @Autowired
    SessionService sessionService;
    @Autowired
    DingTalkConfigService dingTalkConfigService;
    @Autowired
    LicenceCache licenceCache;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DingTalkConfig> add(@RequestBody @Valid String dingTalkConfigJson) throws Exception {
        DingTalkConfig dingTalkConfig = JsonObjUtils.json2obj(dingTalkConfigJson, DingTalkConfig.class);

        // 微信公众号数量是否超过
        LicenceInfo licence = licenceCache.getLicenceInfo();
        if (null == licence) {
            return new BaseResult("-1", "请升级授权", null);
        }

        QueryWrapper<DingTalkConfig> wxQuery = new QueryWrapper();
        wxQuery.eq("is_valid", 1);
        long dingtalkNum = dingTalkConfigService.count(wxQuery);
        if (null != licence.getAuthDingtalk() && dingtalkNum >= licence.getAuthDingtalk()) {
            return new BaseResult("0", "钉钉超过授权数量，请升级授权", null);
        }

        dingTalkConfig.setIsValid(1);
        dingTalkConfig.setUpdateTime(new Date());
        dingTalkConfigService.save(dingTalkConfig);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DingTalkConfig> update(@RequestBody String dingTalkConfigJson) throws Exception {
        DingTalkConfig dingTalkConfig = JsonObjUtils.json2obj(dingTalkConfigJson, DingTalkConfig.class);
        dingTalkConfigService.updateById(dingTalkConfig);
        return new BaseResult("1", "成功", dingTalkConfig);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        dingTalkConfigService.removeById(id);
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
        List<Integer> dingTalkConfigList = new ArrayList<>();
        for (Integer id : ids) {
            dingTalkConfigList.add(id);
        }
        dingTalkConfigService.removeByIds(dingTalkConfigList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DingTalkConfig> get(@RequestBody String id) {
        DingTalkConfig dingTalkConfig = dingTalkConfigService.getById(id);
        return new BaseResult("1", "成功", dingTalkConfig);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<DingTalkConfig>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<DingTalkConfig> page = new Page<DingTalkConfig>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("name") && !"".equals(queryParams.get("name").toString())) {
                wrapper.like("name", queryParams.get("name"));
            }
        }
        page = dingTalkConfigService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
