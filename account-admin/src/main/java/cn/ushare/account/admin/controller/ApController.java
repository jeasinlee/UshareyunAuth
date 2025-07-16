package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.ApService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.Ap;
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
@Api(tags = "ApController", description = "Ap")
@RestController
@Slf4j
@RequestMapping("/ap")
public class ApController {

    private final static String moduleName = "AP列表";

    @Autowired
    SessionService sessionService;
    @Autowired
    ApService apService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<Ap> add(@RequestBody @Valid String apJson) throws Exception {
        Ap ap = JsonObjUtils.json2obj(apJson, Ap.class);
        ap.setIsValid(1);
        ap.setUpdateTime(new Date());
        apService.save(ap);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<Ap> update(@RequestBody String apJson) throws Exception {
        Ap ap = JsonObjUtils.json2obj(apJson, Ap.class);
        apService.updateById(ap);
        return new BaseResult("1", "成功", ap);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        apService.removeById(id);
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
        List<Ap> apList = new ArrayList<>();
        for (Integer id : ids) {
            Ap ap = new Ap();
            ap.setId(id);
            ap.setIsValid(0);
            apList.add(ap);
        }
        apService.updateBatchById(apList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<Ap> get(@RequestBody String id) {
        Ap ap = apService.getById(id);
        return new BaseResult("1", "成功", ap);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询详情", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult<BasePageResult<Ap>> getList(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<Ap> page = new Page<Ap>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = apService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
