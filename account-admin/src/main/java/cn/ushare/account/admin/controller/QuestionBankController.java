package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.QuestionBankService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.dto.QuestionInfoReq;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.QuestionBank;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

/**
 * @author jixiang.li
 * @since 2021-09-01
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "QuestionBankController", description = "题库管理")
@RestController
@Slf4j
@RequestMapping("/questionBank")
public class QuestionBankController {

    private final static String moduleName = "题库管理";

    @Autowired
    QuestionBankService questionBankService;
    @Autowired
    SystemConfigService systemConfigService;

    @ApiOperation(value="新增", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<QuestionBank> add(@RequestBody @Valid String questionBankJson) throws Exception {
        QuestionBank questionBank = JsonObjUtils.json2obj(questionBankJson, QuestionBank.class);
        questionBank.setIsValid(1);
        questionBank.setUpdateTime(new Date());
        questionBankService.save(questionBank);
        return new BaseResult();
    }

    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<QuestionBank> update(@RequestBody String questionBankJson) throws Exception {
        QuestionBank questionBank = JsonObjUtils.json2obj(questionBankJson, QuestionBank.class);
        questionBankService.updateById(questionBank);
        return new BaseResult("1", "成功", questionBank);
    }

    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        questionBankService.removeById(id);
        return new BaseResult();
    }

    @ApiOperation(value="批量删除", notes="")
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<Integer> questionBankList = new ArrayList<>();
        for (Integer id : ids) {
            questionBankList.add(id);
        }
        questionBankService.removeByIds(questionBankList);
        return new BaseResult();
    }

    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<QuestionBank> get(@RequestBody String id) {
        QuestionBank questionBank = questionBankService.getById(id);
        return new BaseResult("1", "成功", questionBank);
    }

    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult<BasePageResult<QuestionBank>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<QuestionBank> page = new Page<>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("question") && !"".equals(queryParams.get("question").toString())) {
                wrapper.like("question", queryParams.get("question"));
            }
        }
        page = questionBankService.getList(page, wrapper);
        return new BaseResult(page);
    }


    @ApiOperation(value="查询配置", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping("/getConfig")
    public BaseResult getConfig() {
        String questionStyle = systemConfigService.getByCode("QUESTION_STYLE");
        String questionCount = systemConfigService.getByCode("QUESTION_COUNT");
        String questionThreshold = systemConfigService.getByCode("QUESTION_THRESHOLD");
        String questionCollection = systemConfigService.getByCode("QUESTION_COLLECTION");
        Map<String, Object> map = new HashMap<>();
        map.put("questionStyle", questionStyle);
        map.put("questionCount", questionCount);
        map.put("questionThreshold", questionThreshold);
        map.put("questionCollection", questionCollection);

        return new BaseResult("1", "成功", map);
    }


    @ApiOperation(value="更新配置", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value = "/updateConfig",method={RequestMethod.POST})
    public BaseResult updateConfig(@RequestBody QuestionInfoReq param) {
        systemConfigService.updateByCode("QUESTION_STYLE", param.getQuestionStyle());
        systemConfigService.updateByCode("QUESTION_COUNT", param.getQuestionCount());
        systemConfigService.updateByCode("QUESTION_THRESHOLD", param.getQuestionThreshold());
        systemConfigService.updateByCode("QUESTION_THRESHOLD", param.getQuestionThreshold());
        systemConfigService.updateByCode("QUESTION_COLLECTION", param.getQuestionCollection());

        return new BaseResult();
    }

    @ApiOperation(value="查询认证题库", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping("/getAuth")
    public BaseResult getAuth() {
        String questionStyle = systemConfigService.getByCode("QUESTION_STYLE");
        String questionCollection = systemConfigService.getByCode("QUESTION_COLLECTION");
        String questionCount = systemConfigService.getByCode("QUESTION_COUNT");
        String questionThreshold = systemConfigService.getByCode("QUESTION_THRESHOLD");
        Map<String, Object> map = new HashMap<>();
        map.put("questionStyle", questionStyle);
        map.put("questionCollection", questionCollection);
        map.put("questionCount", questionCount);
        map.put("questionThreshold", questionThreshold);

        int count = Integer.parseInt(questionCount);
        int threshold = Integer.parseInt(questionThreshold);
        List<QuestionBank> alls = questionBankService.list(new QueryWrapper<>());
        if(CollectionUtils.isEmpty(alls) || count>alls.size() || threshold>count){
            return new BaseResult("-1", "对不起，题库数量设置不正确", null);
        }

        QueryWrapper<QuestionBank> wrapper = new QueryWrapper<>();
        if("0".equals(questionCollection)) {
            wrapper.in("id", Arrays.asList(questionCollection.split(",")));
            wrapper.orderByDesc("id");
            List<QuestionBank> authData = questionBankService.getAuthData(wrapper);
            map.put("authData", authData);
        } else {
            List<QuestionBank> authData = questionBankService.getAuthData2(Integer.parseInt(questionCount));
            map.put("authData", authData);
        }

        return new BaseResult("1", "成功", map);
    }

}
