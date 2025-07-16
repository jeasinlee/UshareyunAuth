package cn.ushare.account.util;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OssClient {

    private static final Logger logger = LoggerFactory.getLogger(OssClient.class);

    private static final String END_POINT = "http://oss-cn-hangzhou.aliyuncs.com";

    private static OSSClient ossClient;

    public OssClient() {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setMaxConnections(200);
        conf.setConnectionTimeout(5000);
        conf.setSocketTimeout(5000);
        conf.setMaxErrorRetry(5);
        ossClient = new OSSClient(END_POINT, getAccessKeyId(), getAccessKeySecret(), conf);
    }

    public static String getBucketName() {
        return "jiabei-file";
    }

    private String getAccessKeyId() {
        return "XXXXXXXXXXXXXXX";
    }

    private String getAccessKeySecret() {
        return "XXXXXXXXXXXXXXXXXXXXXXXXX";
    }

    // ===================================通用方法================================== //

    /**
     * 上传二进制文件到OSS
     *
     * @param inputStream 文件输入流
     * @param bucketName
     * @param dirName
     * @param fileName
     * @return
     */
    public boolean uploadFile(InputStream inputStream, String bucketName, String dirName, String fileName) {
        try {
            PutObjectResult result = ossClient.putObject(bucketName, getFilePath(dirName, fileName), inputStream);
            return result != null;
        } catch (Exception e) {
            logger.error("upload file to oss error,dirName:"
                    + dirName + ",fileName:" + fileName, e);
        }
        return false;
    }

    /**
     * 上传指定文件到OSS上
     *
     * @param fileContent 文件内容
     * @param bucketName
     * @param dirName
     * @param fileName
     * @return
     */
    public boolean uploadFile(String fileContent, String bucketName, String dirName, String fileName) {
        try {
            byte[] data = fileContent.getBytes("utf-8");
            // 获取指定文件的输入流
            InputStream content = new ByteArrayInputStream(data);
            // 创建上传Object的Metadata
            ObjectMetadata meta = new ObjectMetadata();
            // 必须设置ContentLength
            meta.setContentLength(data.length);
            meta.setContentType("application/json");
            // 上传Object.
            PutObjectResult result = ossClient.putObject(
                    bucketName, getFilePath(dirName, fileName), content, meta);
            return result != null;
        } catch (Exception e) {
            logger.error("upload file to oss error,dirName:"
                    + dirName + ",fileName:" + fileName, e);
        }
        return false;
    }

    /**
     * 判断文件是否存在于OSS上
     * oss操作异常返回false
     *
     * @param bucketName
     * @param dirName
     * @param fileName
     * @return
     */
    public boolean isFileExist(String bucketName, String dirName, String fileName) {
        try {
            String filePath = getFilePath(dirName, fileName);
            return ossClient.doesObjectExist(bucketName, filePath);
        } catch (Exception e) {
            logger.error("check file exist error,dirName:"
                    + dirName + ",fileName:" + fileName, e);
        }
        return false;
    }

    /**
     * 从oss上获取一个文件
     *
     * @param bucketName
     * @param dirName
     * @param fileName
     * @return
     */
    public String downloadFile(String bucketName, String dirName, String fileName) {
        try {
            String filePath = getFilePath(dirName, fileName);
            OSSObject object = ossClient.getObject(bucketName, filePath);
            return IOUtils.readStreamAsString(object.getObjectContent(), "utf-8");
        } catch (Exception e) {
            logger.error("download file from oss error,dirName:"
                    + dirName + ",fileName:" + fileName, e);
        }
        return null;
    }

    /**
     * 从oss上删除指定文件
     *
     * @param bucketName
     * @param dirName
     * @param fileName
     * @return
     */
    public boolean deleteFile(String bucketName, String dirName, String fileName) {
        try {
            String filePath = getFilePath(dirName, fileName);
            ossClient.deleteObject(bucketName, filePath);
            return true;
        } catch (Exception e) {
            logger.error("download file from oss error,dirName:"
                    + dirName + ",fileName:" + fileName, e);
        }
        return false;
    }

    /**
     * 通过目录名、文件名构造文件路径
     *
     * @param dirName
     * @param fileName
     * @return
     */
    public String getFilePath(String dirName, String fileName) {
        if (StringUtils.equals(StringUtils.trim(dirName), "")) {
            return fileName;
        }
        return dirName + "/" + fileName;
    }

    /**
     * 删除指定目录下包含关键字的文件，最好文件里有时间，用时间当关键字
     *
     * @param dirName
     * @param keyWords 关键字
     */
    public void deleteObjectForKeyWords(String bucketName, String dirName, String keyWords) {
//        dirName = getRootPath() + "/" + dirName;
        ObjectListing objectListing = null;
        try {
            objectListing = ossClient.listObjects(bucketName, dirName);
        } catch (Exception e) {
            logger.error("listObjects file from oss error,dirName:"
                    + dirName, e);
            return;
        }
        for (OSSObjectSummary summary : objectListing.getObjectSummaries()) {
            String key = summary.getKey();
            String[] split = key.split("/");
            String fileName = split[split.length - 1];
            if (!StringUtils.contains(fileName, keyWords)) {
                continue;
            }
            try {
                ossClient.deleteObject(bucketName, key);
            } catch (Exception e) {
                logger.error("delete file from oss error,key:"
                        + key, e);
            }
        }
    }

    public List<String> getAllFileNameUnderDirNameFromOss(String bucketName, String dirName) {
//        dirName = getRootPath() + "/" + dirName;
        ObjectListing objectListing = null;
        List<String> fileNameList = new ArrayList<>();
        try {
            objectListing = ossClient.listObjects(bucketName, dirName);
            for (OSSObjectSummary summary : objectListing.getObjectSummaries()) {
                String key = summary.getKey();
                String[] split = key.split("/");
                String fileName = split[split.length - 1];
                fileNameList.add(fileName);
            }
            return fileNameList;
        } catch (Exception e) {
            logger.error("listObjects file from oss error,dirName:"
                    + dirName, e);
        }
        return Collections.emptyList();
    }

    public static void main(String[] args) {
//        OssClient ossClient = new OssClient();
//        String fileName = "test.txt";
//        ossClient.uploadFile("This is a test.", getBucketName(), "", fileName);
//        String content = ossClient.downloadFile(getBucketName(), "", fileName);
//        System.out.println(content);
    }
}

