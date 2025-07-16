package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.OnlineUserStatisticService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.OnlineUserStatistic;
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
 * @since 2019-04-23
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "OnlineUserStatisticController", description = "")
@RestController
@Slf4j
@RequestMapping("/onlineUserStatistic")
public class OnlineUserStatisticController {

    @Autowired
    SessionService sessionService;
    @Autowired
    OnlineUserStatisticService onlineUserStatisticService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<OnlineUserStatistic> add(@RequestBody @Valid String onlineUserStatisticJson) throws Exception {
        OnlineUserStatistic onlineUserStatistic = JsonObjUtils.json2obj(onlineUserStatisticJson, OnlineUserStatistic.class);
        onlineUserStatistic.setIsValid(1);
        onlineUserStatistic.setUpdateTime(new Date());
        onlineUserStatisticService.save(onlineUserStatistic);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<OnlineUserStatistic> update(@RequestBody String onlineUserStatisticJson) throws Exception {
        OnlineUserStatistic onlineUserStatistic = JsonObjUtils.json2obj(onlineUserStatisticJson, OnlineUserStatistic.class);
        onlineUserStatisticService.updateById(onlineUserStatistic);
        return new BaseResult("1", "成功", onlineUserStatistic);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        OnlineUserStatistic onlineUserStatistic = new OnlineUserStatistic();
        onlineUserStatistic.setId(id);
        onlineUserStatistic.setIsValid(0);
        onlineUserStatisticService.updateById(onlineUserStatistic);
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
        List<OnlineUserStatistic> onlineUserStatisticList = new ArrayList<>();
        for (Integer id : ids) {
            OnlineUserStatistic onlineUserStatistic = new OnlineUserStatistic();
            onlineUserStatistic.setId(id);
            onlineUserStatistic.setIsValid(0);
            onlineUserStatisticList.add(onlineUserStatistic);
        }
        onlineUserStatisticService.updateBatchById(onlineUserStatisticList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<OnlineUserStatistic> get(@RequestBody String id) {
        OnlineUserStatistic onlineUserStatistic = onlineUserStatisticService.getById(id);
        return new BaseResult("1", "成功", onlineUserStatistic);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<OnlineUserStatistic>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<OnlineUserStatistic> page = new Page<OnlineUserStatistic>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = onlineUserStatisticService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
