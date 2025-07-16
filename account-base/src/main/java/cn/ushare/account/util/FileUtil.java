package cn.ushare.account.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by shenofusc on 2018/10/27.
 */
public class FileUtil {

    private static Set<String> IMAGE_TYPE = new HashSet<>(8);

    private static Set<String> VIDEO_TYPE = new HashSet<>(16);

    private OssClient ossClient = new OssClient();

    static {
        // image type
        IMAGE_TYPE.add("PNG");
        IMAGE_TYPE.add("JPG");
        IMAGE_TYPE.add("JPEG");
        IMAGE_TYPE.add("BMP");
        IMAGE_TYPE.add("TIFF");
        IMAGE_TYPE.add("TIF");
        IMAGE_TYPE.add("GIF");
        // video type
        VIDEO_TYPE.add("AVI");
        VIDEO_TYPE.add("RMVB");
        VIDEO_TYPE.add("WMV");
        VIDEO_TYPE.add("MP4");
        VIDEO_TYPE.add("MKV");
        VIDEO_TYPE.add("ASF");
        VIDEO_TYPE.add("RM");
        VIDEO_TYPE.add("DIVX");
        VIDEO_TYPE.add("MPG");
        VIDEO_TYPE.add("MPEG");
        VIDEO_TYPE.add("MPE");
        VIDEO_TYPE.add("VOB");
    }

    private boolean isImage(String fileType) {
        return IMAGE_TYPE.contains(fileType.toUpperCase());
    }

    private boolean isVideo(String fileType) {
        return VIDEO_TYPE.contains(fileType.toUpperCase());
    }

//    public String upload(HttpServletRequest request) throws IOException {
//        StandardMultipartHttpServletRequest req = (StandardMultipartHttpServletRequest) request;
//        Iterator<String> iterator = req.getFileNames();
//        while (iterator.hasNext()) {
//            MultipartFile reqFile = req.getFile(iterator.next());
//            // 判断文件类型，根据文件类型存入对应的目录
//            Tika tika = new Tika();
//            String fileType = tika.detect(reqFile.getInputStream());
//            fileType = fileType.substring(fileType.lastIndexOf("/") + 1, fileType.length());
//            String fileName = UUID.randomUUID() + "." + fileType.toLowerCase();
//            // 上传文件到OSS
//            if (isImage(fileType)) {
//                String imageDir = "picture";
//                if (ossClient.uploadFile(reqFile.getInputStream(), OssClient.getBucketName(), imageDir, fileName)) {
//                    return imageDir + "/" + fileName;
//                }
//            } else if (isVideo(fileType)) {
//                String videoDir = "video";
//                if (ossClient.uploadFile(reqFile.getInputStream(), OssClient.getBucketName(), videoDir, fileName)) {
//                    return videoDir + "/" + fileName;
//                }
//            }
//        }
//        return null;
//    }

    public boolean delete(String filePath) {
        return ossClient.deleteFile(OssClient.getBucketName(), "", filePath);
    }

}
