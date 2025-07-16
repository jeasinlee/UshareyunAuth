package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.*;
import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.dto.LicenceApplyCode;
import cn.ushare.account.dto.LicenceApplyParam;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.AfterSale;
import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.AESUtil;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.RSAEncryption;
import cn.ushare.account.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class LicenceServiceImpl implements LicenceService {

    @Value("${path.licencePath}")
    String licencePath;

    String licenceName = "licence.ushare";
    String tempLicenceName = "tempLicence.ushare";

    @Autowired
    LicenceCache licenceCache;
    @Autowired
    AcService acService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    SystemCmdService systemCmdService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    WxConfigService wxConfigService;

    /**
     * 获取申请码
     */
    @Override
    public BaseResult<String> getApplyCode() {
        // 硬件设备序列号
        Map<String, String> map = systemCmdService.getHardwareSn();
        String cpuId = (String) map.get("cpuId");
        String mainboardId = (String) map.get("mainboardId");
        String osId = systemCmdService.getOsSerial();
        LicenceApplyCode applyCode = new LicenceApplyCode();
        applyCode.setCpuSerial(cpuId);
        applyCode.setMainBoardSerial(mainboardId);
        applyCode.setOsSerial(osId);
        String applyJson = JSON.toJSONString(applyCode);
        log.debug("applyCode " + applyJson);

        String pubEncrypt = null;
        try {
            pubEncrypt = RSAEncryption.encryptByPubKey(applyJson).replaceAll("[+]", "@");
        } catch (Exception e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", "RSA解密失败", e.getMessage());
        }
        log.debug("申请码:" + pubEncrypt);

        return new BaseResult(pubEncrypt);
    }

    /**
     * 在线授权
     */
    @Override
    public BaseResult onlineLicence(LicenceApplyParam licenceApplyParam) {
        try {
            // 调用获取授权接口
            BaseResult result = requestLicence(licenceApplyParam.getApplyCode(), licenceApplyParam.getPhone());
            if (result.getReturnCode().equals("0")) {
                log.info("===授权：" + result.returnMsg);
                return result;
            }
            String reqUrl = systemConfigService.getByCode("OFFICIAL_SERVER_URL");
            // 根据返回的远程授权文件地址，下载授权文件到本地服务器
            String licenceFileUrl = (String) result.getData();
            result = saveLicence(reqUrl + "/license/" + licenceFileUrl);
            if (result.getReturnCode().equals("0")) {
                return result;
            }
            // 解析本地授权文件，存入缓存
            String localLicenceFile = (String) result.getData();
            result = parseLicence(localLicenceFile);
            if (result.getReturnCode().equals("0")) {
                return result;
            }
        } catch (Exception e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", "在线授权失败", e.getMessage());
        }

        return new BaseResult();
    }

    @Override
    public BaseResult offlineLicence() {
        // 临时文件替换正式授权文件
        replaceLicenceFile();
        // 解析正式授权文件
        BaseResult result = parseLicence(licenceName);
        return result;
    }

    /**
     * 请求官网服务器，获取Licence文件
     */
    BaseResult requestLicence(String applyCode, String phone) {
        // 获取请求地址
        String reqUrl = systemConfigService.getByCode("OFFICIAL_SERVER_URL") + "/createLicense";

        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            post = new HttpPost(reqUrl);
            post.setHeader("Accept", "application/json; charset=utf-8");
            // 构建消息实体
            List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
            pairList.add(new BasicNameValuePair("applyCode", applyCode));
            pairList.add(new BasicNameValuePair("phone", phone));
            post.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));

            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.info("请求出错: " + statusCode);
                return new BaseResult("0", "授权接口请求失败" + statusCode, null);
            } else {
                String resultStr = EntityUtils.toString(response.getEntity(), "utf-8");
                log.debug("licence request resp " + resultStr);
                if (StringUtil.isBlank(resultStr)) {
                    return new BaseResult("0", "授权接口返回为空", null);
                } else {
                    JSONObject resultMap = JSONObject.fromObject(resultStr);
                    if (resultMap.get("code") == null) {
                        return new BaseResult("0", "授权返回参数错误", null);
                    }
                    int resultCode = resultMap.optInt("code");
                    if (resultCode != 100) {
                        if (resultMap.get("message") != null) {
                            return new BaseResult("0", (String) resultMap.get("message"), null);
                        } else {
                            return new BaseResult("0", "授权失败", null);
                        }
                    }
                    // 返回文件地址
                    String fileUrl = resultMap.optString("data");
                    if (!"null".equals(fileUrl) && StringUtil.isBlank(fileUrl)) {
                        return new BaseResult("0", "授权文件为空", null);
                    }
                    return new BaseResult(fileUrl);
                }
            }
        } catch (Exception e) {
            log.error("Error Exception=", e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
        return new BaseResult();
    }

    /**
     * 解析Licence文件，并存入缓存
     */
    @Override
    public BaseResult parseLicence(String fileName) {
        String filePathName = licencePath + "/" + fileName;
        File file = new File(filePathName);
        if (!file.exists()) {
            return new BaseResult("0", "没有授权文件", null);
        }
        Long filelength = file.length();
        byte[] fileContent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(fileContent);
            in.close();
        } catch (FileNotFoundException e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", "授权文件读取失败", null);
        } catch (IOException e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", "授权文件读取失败", null);
        }

        try {
            String dataStr = new String(fileContent, "UTF-8");
            String[] dataArray = dataStr.split("\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*");
            String encryptLicence = dataArray[0];
            String encryptAfterSale = dataArray[1];
            String encryptAesKey = dataArray[2];

            // Rsa解密
            String licenceJson = RSAEncryption.decryptByPubKey(encryptLicence);
            String aesKey = RSAEncryption.decryptByPubKey(encryptAesKey);

            // json转obj
            LicenceInfo licenceInfo = JSON.parseObject(licenceJson, LicenceInfo.class);

            // AES解密购买的服务
            log.debug("aesKey " + aesKey + " encryptAfterSale " + encryptAfterSale);
            String afterSaleJson = AESUtil.AESDncode(aesKey, encryptAfterSale);
            //String afterSaleJson = AESUtil.AESDncode("6adaffed-8763-11e9-a65d-276fc89de398", "Kdx97kRqlnjXn50UVf+RIMAgEOUYfgt4Ha7XT+qJUyiPQ3lvQhw2Yh7Iwp6qF3TdQpfesppYJYX/7C8WhY0Q5ZCj+hF7oxlEbtHY7m8WT2mivM2QQyVKXB0+FUmpqlhJ81XgRLMd9P6cO+fhSsWt8BOUO69t5j++AH7LYShFx0GveNbNjQD8BAL4L9tS2mDty9Cwe5hTF7kK9gDgOGGE6bkiLIWJ9QMvsw6n20Kc4etcGfr+IBsroXQJigOQGhayyT3BkGsuPbknJu73zMV4lQ==");
            log.debug("servicesJson " + afterSaleJson);
            List<AfterSale> afterSaleList = (List<AfterSale>) JSON.parseArray(afterSaleJson, AfterSale.class);

            // 对比硬件序列号
            Map<String, String> serialMap = systemCmdService.getHardwareSn();
            String cpuId = serialMap.get("cpuId");
            String mainboardId = serialMap.get("mainboardId");
            String osId = systemCmdService.getOsSerial();
            if (!licenceInfo.getCpuSerial().equals(cpuId)
                || !licenceInfo.getMainBoardSerial().equals(mainboardId)
                || !licenceInfo.getOsSerial().equals(osId)) {
                return new BaseResult("0", "授权文件硬件不匹配", null);
            }

            // 解析后数据存入缓存
            licenceCache.addOrUpdate(licenceInfo, afterSaleList);

            // 返回map
            Map<String, Object> map = new HashMap<>();
            map.put("licenceInfo", licenceInfo);
            map.put("afterSaleList", afterSaleList);
            return new BaseResult(map);
        } catch (UnsupportedEncodingException e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", "授权文件读取失败", e.getMessage());
        } catch (Exception e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", "授权文件读取失败", e.getMessage());
        }
    }

    /**
     * 检查是否配置行为管理
     * @return
     */
    @Override
    public BaseResult checkActionConfig(){
        LicenceInfo licence = licenceCache.getLicenceInfo();
        if (null == licence) {
            return new BaseResult("-1", "请升级授权", null);
        }

        BaseResult result = new BaseResult();
        if(null == licence.getAuthAction() || licence.getAuthAction()==0){
            result.setReturnCode("-1");
        }

        return result;
    }

    /**
     * 检查授权
     */
    @Override
    public BaseResult checkInfo() {
        LicenceInfo licence = licenceCache.getLicenceInfo();
        if (null == licence) {
            return new BaseResult("-1", "请升级授权", null);
        }

        //是否过期
        if(licence.getExpireTime().before(new Date())){
            return new BaseResult("-1", "授权已过期，请联系厂商", null);
        }

        //是否被禁用
        String authServerValid = systemConfigService.getByCode("AUTH_SERVER_VALID");
        if(!"1".equals(authServerValid)){
            return new BaseResult("-1", "系统被禁用，请联系管理员.", null);
        }

        // 检查AC是否超过授权数量
        QueryWrapper<Ac> acQuery = new QueryWrapper();
        acQuery.eq("is_valid", 1);
        int acNum = acService.count(acQuery);
        if (acNum > licence.getAcAmount()) {
            return new BaseResult("-2", "控制器超过授权数量，请升级授权", null);
        }

        // 检查认证用户数是否超过授权数量
        QueryWrapper<AuthUser> userQuery = new QueryWrapper();
        userQuery.eq("online_state", 1);
        userQuery.eq("is_valid", 1);
        int userNum = authUserService.count(userQuery);
        if (userNum > licence.getStaAmount()) {
            return new BaseResult("-3", "认证用户超过授权数量，请升级授权", null);
        }

        return new BaseResult();
    }

    @Override
    public LicenceInfo getAccountInfo() {
        return licenceCache.getLicenceInfo();
    }

    /**
     * 保存远端授权文件
     */
    BaseResult saveLicence(String fileUrl) throws IOException {
        URL urlfile = null;
        HttpURLConnection httpUrl = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        File filePath = new File(licencePath);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        String filePathName = licencePath + "/" + licenceName;
        File f = new File(filePathName);
        try {
            urlfile = new URL(fileUrl);
            httpUrl = (HttpURLConnection) urlfile.openConnection();
            httpUrl.connect();
            bis = new BufferedInputStream(httpUrl.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(f));
            int len = 2048;
            byte[] b = new byte[len];
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            bos.flush();
            bis.close();
            httpUrl.disconnect();
        } catch (Exception e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", "授权文件下载失败", e.getMessage());
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (IOException e) {
                log.error("Error Exception=", e);
            }
        }
        return new BaseResult(licenceName);
    }

    /**
     * 临时文件替换正式文件
     */
    private void replaceLicenceFile() {
        //存文件
        FileInputStream inputStream = null;
        File tempFile = new File(licencePath + "/" + tempLicenceName);
        File newFile = new File(licencePath + "/" + licenceName);
        BufferedOutputStream out;
        try {
            inputStream = new FileInputStream(tempFile);
            out = new BufferedOutputStream(new FileOutputStream(newFile));
            int b = 0;
            byte[] buffer = new byte[512];
            while (b != -1) {
                b = inputStream.read(buffer);
                if (b != -1) {
                    out.write(buffer, 0, b);
                }
            }
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            log.error("Error Exception=", e);
        } catch (IOException e) {
            log.error("Error Exception=", e);
        }
    }

    /**
     * 查询软件版本，包括本地版本和最新版本
     */
    @Override
    public BaseResult getSoftwareVersion() {
        String localVersionCode = systemConfigService.getByCode("SOFTWARE-VERSION");
        String officialServerUrl = systemConfigService.getByCode("OFFICIAL_SERVER_URL");

        String osName = System.getProperty("os.name");
        String sysType = null;
        if (osName.matches("^(?i)Windows.*$")) {
            sysType = "win";
        } else {
            sysType = "linux";
        }
        // 获取请求地址
        String reqUrl = officialServerUrl + "/api/last_version?sysType=" + sysType;

        HttpPost post = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            post = new HttpPost(reqUrl);
            post.setHeader("Accept", "application/json; charset=utf-8");
            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.info("请求出错: " + statusCode);
                return new BaseResult("0", "最新软件版本接口请求失败" + statusCode, null);
            } else {
                String resultStr = EntityUtils.toString(response.getEntity(), "utf-8");
                log.debug("resultStr " + resultStr);
                if (StringUtil.isBlank(resultStr)) {
                    return new BaseResult("0", "最新软件版本接口返回为空", null);
                } else {
                    Map<String, Object> resultMap = (Map<String, Object>) JsonObjUtils.json2map(resultStr);
                    if (resultMap.get("returnCode") == null) {
                        return new BaseResult("0", "最新软件版本返回参数错误", null);
                    }
                    String resultCode = resultMap.get("returnCode").toString();
                    if (!"1".equals(resultCode)) {
                        if (resultMap.get("returnMsg") != null) {
                            return new BaseResult("0", (String) resultMap.get("returnMsg"), null);
                        } else {
                            return new BaseResult("0", "最新软件版本查询失败", null);
                        }
                    }
                    // 返回版本号
                    JSON dataJson = (JSON) resultMap.get("data");
                    Map<String, Object> dataMap = (Map<String, Object>) JsonObjUtils.json2map(dataJson.toJSONString());
                    dataMap.put("localVersionCode", localVersionCode);
                    dataMap.put("showNew", StringUtil.compareVersion(localVersionCode, dataMap.get("versionCode").toString())?1:0);
                    dataMap.put("downloadUrl", dataMap.get("downloadUrl"));
                    return new BaseResult(dataMap);
                }
            }
        } catch (Exception e) {
            log.error("Error Exception=", e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }

        // 如果远程版本查询失败，则只返回本地版本
        Map<String, Object> map = new HashMap<>();
        map.put("localVersionCode", localVersionCode);
        return new BaseResult(map);
    }




}
