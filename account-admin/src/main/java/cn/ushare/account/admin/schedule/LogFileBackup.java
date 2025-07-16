package cn.ushare.account.admin.schedule;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import cn.ushare.account.admin.service.LicenceService;
import cn.ushare.account.admin.service.LogUploadRecordService;
import cn.ushare.account.admin.service.SystemConfigService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.LogUploadRecord;
import cn.ushare.account.util.DateTimeUtil;
import cn.ushare.account.util.JsonObjUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志文件备份
 */
@Configuration
@EnableScheduling
@Slf4j
public class LogFileBackup {

    @Autowired
    LogUploadRecordService recordService;
    @Autowired
    SystemConfigService configService;
    @Autowired
    LicenceService licenceService;
    @Autowired
    LicenceCache licenceCache;

    @Value("${path.logPath}")
    String logPath;

    private static boolean inProcess = false;

    @Scheduled(cron = "${schedule.logFileBackupTime}")
    public void scheduler() throws Exception {
        if (inProcess) {
            return;
        }
        inProcess = true;
        try {
            // 查询授权，没有授权则不用上传备份
            BaseResult licenceCheckResult = licenceService.checkInfo();
            if (!licenceCheckResult.getReturnCode().equals("1")) {
                inProcess = false;
                return;
            }
            
            LicenceInfo licenceInfo = licenceCache.getLicenceInfo();
            String phone = null != licenceInfo ? licenceInfo.getAccount() : "";
            
            String status = configService.getByCode("LOG-BAKUP-STATUS");
            String daysStr = configService.getByCode("LOG-BAKUP-SAVE-DAYS");
            Integer days = Integer.valueOf(daysStr);
            String url = configService.getByCode("LOG-BAKUP-CLOUD-URL");
            if (status.equals("0")) {// 日志存档功能已关闭
                inProcess = false;
                return;
            }

            // 计算过期文件时间
            Long expireTime = (new Date()).getTime() - ((long) days) * 24 * 3600 * 1000;
            String expireDateStr = DateTimeUtil.date_sdf.format(new Date(expireTime));

            // 查询文件列表
            File file = new File(logPath);
            if (file.exists()) {
                LinkedList<File> list = new LinkedList<File>();
                File[] files = file.listFiles();
                for (File subFile : files) {
                    if (!subFile.isDirectory()) {
                        String fileName = subFile.getName();
                        // 是否已上传
                        QueryWrapper<LogUploadRecord> queryWrapper = new QueryWrapper();
                        queryWrapper.eq("name", fileName);
                        queryWrapper.eq("is_valid", 1);
                        LogUploadRecord record = recordService.getOne(queryWrapper);
                        if (record != null) {// 文件已上传
                            // 过期文件，删除
                            String prefixName = fileName.substring(0, 10);
                            if (prefixName.compareTo(expireDateStr) < 0) {
                                subFile.delete();
                            }
                        } else {// 文件没上传过，压缩上传
                            boolean result = false;
                            try {
                                result = uploadFile(subFile, url, phone, days);                                
                            } catch (Exception e) {
                                log.error("Error Exception=", e);
                                return;
                            }

                            if (result) {
                                // 新增上传记录
                                record = new LogUploadRecord();
                                record.setName(fileName);
                                record.setUploadUrl(url);
                                record.setIsValid(1);
                                recordService.save(record);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error Exception=", e);
            inProcess = false;
        }
        inProcess = false;
    }

    /**
     * 压缩并上传文件，
     * 正在写入的文件，上传会失败，后端接口提示Failed to parse multipart servlet request，
     * 并不是前端参数错误，是后台读取这个文件冲突
     */
    boolean uploadFile(File file, String url, String phone, Integer saveDays) {
        boolean result = true;
        // 压缩

        // 上传
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("uploadFile", new FileBody(file))
                    .addPart("phone", new StringBody(phone, ContentType.create("text/plain", "utf-8")))
                    .addPart("expiredDays", new StringBody(saveDays + "", ContentType.create("text/plain", "utf-8")))
                    .build();
            httpPost.setEntity(reqEntity);

            // 发起请求 并返回请求的响应
            response = httpClient.execute(httpPost);
            //System.out.println("The response value of token:" + response.getFirstHeader("token"));
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.error("日志上传接口状态码错误，请检查网络");
            }
            
            // 获取响应对象
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String resp = EntityUtils.toString(resEntity, "utf-8");
                log.debug("log cloud backup resp " + resp);
                Map<String, Object> map = (Map<String, Object>) JsonObjUtils.json2map(resp);
                Integer code = (Integer) map.get("code");
                String message = (String) map.get("message");
                if (code != 100) {
                    result = false;
                    log.error("log backup error " + message);
                }                
            }

            // 销毁
            EntityUtils.consume(resEntity);
        } catch (Exception e) {
            result = false;
            log.error("Error Exception=", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                result = false;
                log.error("Error Exception=", e);
            }
        }
        return result;
    }

}
