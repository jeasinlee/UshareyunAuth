package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.DbBackupRecordService;
import cn.ushare.account.admin.service.SystemCmdService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.entity.DbBackupRecord;
import cn.ushare.account.util.DateTimeUtil;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BasePage;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.admin.mapper.DbBackupRecordMapper;
import cn.ushare.account.dto.LicenceInfo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
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
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jixiang.li
 * @since 2019-04-30
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class DbBackupRecordServiceImpl extends ServiceImpl<DbBackupRecordMapper, DbBackupRecord> implements DbBackupRecordService {

    @Value("${path.dbSavePath}")
    String dbSavePath;
    @Autowired
    DbBackupRecordMapper dbBackupRecordMapper;
    @Autowired
    SystemCmdService systemCmdService;
    @Autowired
    LicenceCache licenceCache;
    @Autowired
    SystemConfigService configService;

    @Override
    public Page<DbBackupRecord> getList(Page<DbBackupRecord> page, QueryWrapper wrapper) {
        return page.setRecords(dbBackupRecordMapper.getList(page, wrapper));
    }

    @Override
    public BaseResult localBackup() throws InterruptedException {
        String fileName = (DateTimeUtil.yyyymmddHHmmss).format(new Date()) + ".sql";
        BaseResult result = systemCmdService.exportDb(dbSavePath, fileName);
        if (result.getReturnCode().equals("0")) {
            return result;
        }

        // 新增记录
        DbBackupRecord record = new DbBackupRecord();
        record.setName(fileName);
        record.setPath(dbSavePath);
        Integer fileSize = (Integer) result.getData();
        record.setFileSize(fileSize);
        record.setIsValid(1);
        dbBackupRecordMapper.insert(record);

        return new BaseResult();
    }

    @Override
    public BaseResult localRestore(String fileName) throws InterruptedException {
        BaseResult result = systemCmdService.importDb(dbSavePath, fileName);
        return result;
    }

    @Override
    public BaseResult cloudBackup() throws InterruptedException {
        LicenceInfo licenceInfo = licenceCache.getLicenceInfo();
        if (null == licenceInfo) {
            return new BaseResult("0", "请先升级授权", null);
        }

        String fileName = (DateTimeUtil.yyyymmddHHmmss).format(new Date()) + ".sql";
        BaseResult result = systemCmdService.exportDb(dbSavePath, fileName);
        if (result.getReturnCode().equals("0")) {
            return result;
        }

        String phone = licenceInfo.getAccount();
        String url = configService.getByCode("DB-BAKUP-CLOUD-URL");
        File file = new File(dbSavePath + "/" + fileName);
        result = uploadFile(file, url, phone);

        return result;
    }

    /**
     * 压缩并上传文件，
     * 正在写入的文件，上传会失败，后端接口提示Failed to parse multipart servlet request，
     * 并不是前端参数错误，是后台读取这个文件冲突
     */
    BaseResult uploadFile(File file, String url, String phone) {
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
                    .build();
            httpPost.setEntity(reqEntity);

            // 发起请求 并返回请求的响应
            response = httpClient.execute(httpPost);
            //System.out.println("The response value of token:" + response.getFirstHeader("token"));
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.error("数据库云存储接口状态码错误，请检查网络");
            }

            // 获取响应对象
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String resp = EntityUtils.toString(resEntity, "utf-8");
                log.debug("db cloud backup resp " + resp);
                Map<String, Object> map = (Map<String, Object>) JsonObjUtils.json2map(resp);
                Integer code = (Integer) map.get("code");
                String message = (String) map.get("message");
                if (code != 100) {
                    log.error("db cloud backup error " + message);
                    return new BaseResult("0", message, null);
                }
            }

            // 销毁
            EntityUtils.consume(resEntity);
        } catch (Exception e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", "db cloud backup exception " + e.getMessage(), null);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                log.error("Error Exception=", e);
                return new BaseResult("0", "db cloud backup exception " + e.getMessage(), null);
            }
        }
        return new BaseResult();
    }

}
