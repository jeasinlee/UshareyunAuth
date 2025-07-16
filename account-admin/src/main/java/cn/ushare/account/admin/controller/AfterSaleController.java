package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AfterSaleService;
import cn.ushare.account.admin.service.FileUploadService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AfterSale;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AfterSaleController", description = "")
@RestController
@Slf4j
@RequestMapping("/afterSale")
public class AfterSaleController {

    @Value("${path.licencePath}")
    String licencePath;

    @Autowired
    SessionService sessionService;
    @Autowired
    AfterSaleService afterSaleService;
    @Autowired
    FileUploadService fileUploadService;

    /**
     * 文件方式续费
     */
    @ApiOperation(value="文件方式续费", notes="")
    @RequestMapping(value="/fileUpdate", method={RequestMethod.POST})
    public BaseResult<AfterSale> fileUpdate(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        return fileUploadService.upload(file, licencePath, fileName);
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<AfterSale> update(@RequestBody String afterSaleJson) throws Exception {
        AfterSale afterSale = JsonObjUtils.json2obj(afterSaleJson, AfterSale.class);
        afterSaleService.updateById(afterSale);
        return new BaseResult("1", "成功", afterSale);
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AfterSale> get(@RequestBody String id) {
        AfterSale afterSale = afterSaleService.getById(id);
        return new BaseResult("1", "成功", afterSale);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<AfterSale> page = new Page<AfterSale>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = afterSaleService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
