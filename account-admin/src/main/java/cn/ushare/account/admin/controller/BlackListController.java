package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.BlackListService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.BlackList;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "BlackListController", description = "黑名单")
@RestController
@Slf4j
@RequestMapping("/blackList")
public class BlackListController {

    private final static String moduleName = "黑名单";

    @Autowired
    SessionService sessionService;
    @Autowired
    BlackListService blackListService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BlackList> add(@RequestBody @Valid String blackListJson) throws Exception {
        BlackList blackList = JsonObjUtils.json2obj(blackListJson, BlackList.class);
        return blackListService.add(blackList);
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BlackList> update(@RequestBody String blackListJson) throws Exception {
        BlackList blackList = JsonObjUtils.json2obj(blackListJson, BlackList.class);
        return blackListService.update(blackList);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        blackListService.removeById(id);
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
        List<Integer> blackListList = new ArrayList<>();
        for (Integer id : ids) {
            blackListList.add(id);
        }
        blackListService.removeByIds(blackListList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BlackList> get(@RequestBody String id) {
        BlackList blackList = blackListService.getById(id);
        return new BaseResult("1", "成功", blackList);
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
        Page<BlackList> page = new Page<BlackList>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("type") && !"".equals(queryParams.get("type").toString())) {
                wrapper.like("type", queryParams.get("type"));
            }
            if (null != queryParams.get("value") && !"".equals(queryParams.get("value").toString())) {
                wrapper.like("value", queryParams.get("value"));
            }
        }
        page = blackListService.getList(page, wrapper);

        return new BaseResult(page);
    }

    /**
     * 手机黑名单模板导入
     */
    @ApiOperation(value="手机黑名单模板导入", notes="")
    @SystemLogTag(description="手机黑名单模板导入", moduleName=moduleName)
    @RequestMapping(value="/excelImportPhone", method={RequestMethod.POST})
    public BaseResult excelImportPhone(MultipartFile file) throws Exception {
        return blackListService.excelImportPhone(file);
    }

    /**
     * MAC黑名单模板导入
     */
    @ApiOperation(value="MAC黑名单模板导入", notes="")
    @SystemLogTag(description="MAC黑名单模板导入", moduleName=moduleName)
    @RequestMapping(value="/excelImportMac", method={RequestMethod.POST})
    public BaseResult excelImportMac(MultipartFile file) throws Exception {
        return blackListService.excelImportMac(file);
    }

    /**
     * 模板导出手机
     */
    @ApiOperation(value="模板导出手机", notes="")
    @SystemLogTag(description="手机黑名单模板导出", moduleName=moduleName)
    @RequestMapping(value="/excelExportPhone", method={RequestMethod.GET})
    public void excelExportPhone(String ids) throws Exception {
        blackListService.excelExportPhone(ids);
    }

    /**
     * 模板导出MAC
     */
    @ApiOperation(value="模板导出MAC", notes="")
    @SystemLogTag(description="MAC黑名单模板导出", moduleName=moduleName)
    @RequestMapping(value="/excelExportMac", method={RequestMethod.GET})
    public void excelExportMac(String ids) throws Exception {
        blackListService.excelExportMac(ids);
    }

}
