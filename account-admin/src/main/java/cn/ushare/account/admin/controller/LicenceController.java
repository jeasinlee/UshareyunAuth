package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.FileUploadService;
import cn.ushare.account.admin.service.LicenceService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.dto.LicenceApplyParam;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.AfterSale;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
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

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "LicenceController", description = "Licence授权")
@RestController
@Slf4j
@RequestMapping("/licence")
public class LicenceController {

    private final static String moduleName = "Licence授权";

    @Value("${path.licencePath}")
    String licencePath;

    String licenceName = "licence.ushare";
    String tempLicenceName = "tempLicence.ushare";

    @Autowired
    SessionService sessionService;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    LicenceService licenceService;
    @Autowired
    LicenceCache licenceCache;

    /**
     * 获取申请码
     */
    @ApiOperation(value = "获取申请码", notes = "")
    @SystemLogTag(description="获取申请码", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value = "/getApplyCode", method = { RequestMethod.POST })
    public BaseResult<String> getApplyCode() throws Exception {
        BaseResult result = licenceService.getApplyCode();
        return result;
    }

    /**
     * 在线授权
     */
    @ApiOperation(value = "onlineLicence", notes = "")
    @SystemLogTag(description="在线授权", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value = "/onlineLicence", method = { RequestMethod.POST })
    public BaseResult onlineLicence(@RequestBody String applyParamJson) throws Exception {
        LicenceApplyParam applyParam = JsonObjUtils.json2obj(applyParamJson, LicenceApplyParam.class);
        BaseResult result = licenceService.onlineLicence(applyParam);
        return result;
    }

    /**
     * 离线授权文件上传
     */
    @ApiOperation(value = "uploadOfflineLicence", notes = "")
    @SystemLogTag(description="上传离线授权文件", moduleName=moduleName)
    @RequestMapping(value = "/uploadOfflineLicence", method = { RequestMethod.POST })
    public BaseResult uploadOfflineLicence(MultipartFile file) {
        return fileUploadService.upload(file, licencePath, tempLicenceName);
    }

    /**
     * 离线授权
     */
    @ApiOperation(value = "offlineLicence", notes = "")
    @SystemLogTag(description="离线授权", moduleName=moduleName)
    @RequestMapping(value = "/offlineLicence", method = { RequestMethod.POST })
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult offlineLicence() {
        return licenceService.offlineLicence();
    }

    /**
     * 查询Licence信息
     */
    @ApiOperation(value = "查询Licence信息", notes = "")
    @SystemLogTag(description="授权信息查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value = "/getInfo", method = { RequestMethod.POST })
    public BaseResult<LicenceInfo> getInfo() {
        LicenceInfo info = licenceCache.getLicenceInfo();
        if (null == info) {
            return new BaseResult("0", "没有授权信息", null);
        }
        return new BaseResult(info);
    }

    /**
     * 查询售后服务
     */
    @ApiOperation(value = "查询售后服务", notes = "")
    @SystemLogTag(description="查询售后服务", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value = "/getAfterSale", method = { RequestMethod.POST })
    public BaseResult getAfterSale() {
        List<AfterSale> afterSaleList = licenceCache.getAfterSaleList();
        return new BaseResult(afterSaleList);
    }

    /**
     * 查询软件版本，包括本地版本和最新版本
     */
    @ApiOperation(value = "查询软件版本", notes = "")
    @SystemLogTag(description="查询软件版本", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value = "/getSoftwareVersion", method = { RequestMethod.POST })
    public BaseResult getSoftwareVersion() {
        return licenceService.getSoftwareVersion();
    }

//    /**
//     * 获取迁移申请码
//     */
//    @ApiOperation(value = "获取迁移申请码", notes = "")
//    @RequestMapping(value = "/getMigrateApplyCode", method = { RequestMethod.POST })
//    public BaseResult<String> getMigrateApplyCode() {
//        String temp = "388segesegisegsse-gs388seg388seges-egisegssegs388sege-segisegssegs"
//                + "esegisegssegs-388segesegisegssegs-388segesegisegssegs";
//        return new BaseResult(temp);
//    }
//
//    /**
//     * 在线迁移
//     */
//    @ApiOperation(value = "onlineMigrate", notes = "")
//    @RequestMapping(value = "/onlineMigrate", method = { RequestMethod.POST })
//    public BaseResult<String> onlineMigrate(@RequestBody LicenceApplyCode licenceAuthParam) {
//        return new BaseResult();
//    }
//
//    /**
//     * 离线迁移
//     */
//    @ApiOperation(value = "offlineMigrage", notes = "")
//    @RequestMapping(value = "/offlineMigrate", method = { RequestMethod.POST })
//    public BaseResult<String> offlineMigrate(@RequestBody LicenceApplyCode licenceAuthParam) {
//        return new BaseResult();
//    }
//
//    /**
//     * 迁移
//     */
//    @ApiOperation(value = "迁移", notes = "")
//    @RequestMapping(value = "/migrate", method = { RequestMethod.POST })
//    public BaseResult<LicenceInfo> migrate(MultipartFile file) {
//        String fileName = file.getOriginalFilename();
//        return fileUploadService.upload(file, licencePath, fileName);
//    }

}
