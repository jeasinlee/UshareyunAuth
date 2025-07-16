package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.admin.service.LicenceService;
import cn.ushare.account.admin.service.WxConfigService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.admin.utils.CommonUtil;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.WxConfig;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.IpUtil;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "WxConfigController", description = "微信配置")
@RestController
@Slf4j
@RequestMapping("/wxConfig")
public class WxConfigController {

    private final static String moduleName = "微信配置";

    @Autowired
    SessionService sessionService;
    @Autowired
    WxConfigService wxConfigService;
    @Autowired
    LicenceService licenceService;
    @Autowired
    LicenceCache licenceCache;
    @Value("${weixin.mini.redirectUrl}")
    String redirectUrl;
    @Value("${weixin.mini.selectOpenIdUrl}")
    String selectOpenIdUrl;
    @Value("${weixin.mini.selectAllOpenIdUrl}")
    String selectAllOpenIdUrl;
    @Value("${weixin.mini.deleteOpenIdUrl}")
    String deleteOpenIdUrl;
    @Autowired
    HttpServletRequest request;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<WxConfig> add(@RequestBody @Valid String wxConfigJson) throws Exception {
        WxConfig wxConfig = JsonObjUtils.json2obj(wxConfigJson, WxConfig.class);
        // 微信公众号数量是否超过
        LicenceInfo licence = licenceCache.getLicenceInfo();
        if (null == licence) {
            return new BaseResult("-1", "请升级授权", null);
        }

        QueryWrapper<WxConfig> wxQuery = new QueryWrapper();
        wxQuery.eq("is_valid", 1);
        long wxNum = wxConfigService.count(wxQuery);
        if (null != licence.getAuthWx() && wxNum >= licence.getAuthWx()) {
            return new BaseResult("0", "微信公众号超过授权数量，请升级授权", null);
        }

        wxConfig.setIsValid(1);
        wxConfig.setUpdateTime(new Date());
        wxConfigService.save(wxConfig);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<WxConfig> update(@RequestBody String wxConfigJson) throws Exception {
        WxConfig wxConfig = JsonObjUtils.json2obj(wxConfigJson, WxConfig.class);
        wxConfigService.updateById(wxConfig);
        return new BaseResult("1", "成功", wxConfig);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        wxConfigService.removeById(id);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<Integer> integerList = new ArrayList<>();
        for (Integer id : ids) {
            integerList.add(id);
        }
        wxConfigService.removeByIds(integerList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<WxConfig> get(@RequestBody String id) {
        WxConfig wxConfig = wxConfigService.getById(id);
        return new BaseResult("1", "成功", wxConfig);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<WxConfig>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<WxConfig> page = new Page<WxConfig>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }

        page = wxConfigService.getList(page, wrapper);

        return new BaseResult(page);
    }

    /**
     * 获取url
     * @return
     */
    @ApiOperation(value = "获取url")
//    @CrossOrigin(origins = "*")
    @RequestMapping(value="/getConfig", method={RequestMethod.GET, RequestMethod.POST})
    public void getConfig(HttpServletRequest request, HttpServletResponse response){
        Page<WxConfig> page = new Page<WxConfig>(1, 10);
        Page<WxConfig> list = wxConfigService.getList(page, new QueryWrapper());
        WxConfig wxConfig = list.getRecords().get(0);
        String appId = wxConfig.getAppId();
        String userIp = IpUtil.getIpAddr(request);
        String secret = wxConfig.getAppSecret();
        String encodedRedirectUri = "";
        try {
            encodedRedirectUri = URLEncoder.encode(redirectUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Boolean pc = CommonUtil.isPc(request);
        String isPc = pc ?"1":"0";

        /**
         * send to wechat
         */
//        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+redirectUrl+"&response_type=code&scope=snsapi_base&state="+appId+"|||"+secret+"|||"+userIp+"#wechat_redirect";
        String state = appId+"|||"+secret+"|||"+userIp+"|||"+isPc;

        // URL encode the state parameter
        String encodedState = "";
        try {
            encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" +
                appId +
                "&redirect_uri=" +
                encodedRedirectUri +
                "&response_type=code&scope=snsapi_base&state=" +
                encodedState+
                "#wechat_redirect";


        try {

            response.sendRedirect(url);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        Map map = new HashMap();
//        map.put("response",response);
//        map.put("request",request);
//        map.put("url",realUrl);
//
////        return new BaseResult("200","成功",map);
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//            HttpPost doPost = new HttpPost(url);
//            // 将Map转换为JSON字符串
//            ObjectMapper objectMapper = new ObjectMapper();
//            String json = objectMapper.writeValueAsString(map);
//
//            // 设置请求实体
//            StringEntity entity = new StringEntity(json);
//            doPost.setEntity(entity);
//            doPost.setHeader("Accept", "application/json");
//            doPost.setHeader("Content-type", "application/json");
//
//            HttpResponse postResponse = httpClient.execute(doPost);
//            System.out.println("Response Code : " + postResponse.getStatusLine().getStatusCode());
//
//            String result = EntityUtils.toString(postResponse.getEntity());
//            System.out.println(result);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new BaseResult("200");
//        System.out.println(url);
//        String selectUrl = selectOpenIdUrl+"?ip="+userIp+appId;
//        Map map = new HashMap();
//        map.put("url",realUrl);
//        map.put("selectUrl",selectUrl);


//        return new BaseResult("200","成功",map);
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//            HttpGet request = new HttpGet(url);
//
//            HttpResponse response = httpClient.execute(request);
//            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
//
//            String result = EntityUtils.toString(response.getEntity());
//            System.out.println(result);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        String openId = null;
//        while (openId==null){
//
//            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//                HttpGet request = new HttpGet(selectUrl);
//
//                HttpResponse response = httpClient.execute(request);
//                System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
//
//                String result = EntityUtils.toString(response.getEntity());
//                System.out.println(result);
//                ObjectMapper mapper = new ObjectMapper();
//                JsonNode jsonResponse = mapper.readTree(result);
//
//                JsonNode node = jsonResponse.get("data").get(0).get("openId");
//
//                if (ObjectUtils.isNotEmpty(node)){
//                    openId = node.asText();
//                }
//
//                Thread.sleep(1000);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }

    }
}
