package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AdImageService;
import cn.ushare.account.admin.service.FileUploadService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * @author jixiang.li
 * @date 2018-01-05
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "FileUploadController", description = "文件上传")
@RestController
@Slf4j
@RequestMapping("/fileUpload")
public class FileUploadController {

    @Value("${path.uploadPath}")
    String uploadPath;
    @Value("${path.dbSavePath}")
    String dbSavePath;

    @Autowired
    HttpServletRequest request;
    @Autowired
    AdImageService adImageService;
    @Autowired
    FileUploadService fileUploadService;

	/**
     * 文件上传
     */
	@ApiOperation(value="文件上传", notes="")
    @RequestMapping(value="/common/{subPath}", method={RequestMethod.POST})
	public BaseResult uploadCommonFile(MultipartFile file,
	        @PathVariable("subPath") String subPath) throws Exception {
    	//服务器存放文件路径
//    	String projectPath = req.getServletContext().getRealPath("/");
//    	projectPath = projectPath.replace("\\", "/");
//    	projectPath = projectPath.replace("static/", "");//savedUrl已经有static文件夹路径，这里重复了

    	//原文件路径
    	String fileName = file.getOriginalFilename();
    	String suffix = fileName.substring(fileName.lastIndexOf("."));

    	//新文件存放路径及文件名
    	Date now = new Date();
		//String newFilePath = uploadPath + "static/uploadFile/";
    	String newFilePath = uploadPath;
    	String newFileName = StringUtil.getRandomString(10) + suffix;
    	String relativePath = "/" + subPath + "/" + newFileName;
    	if (null != subPath && !"".equals(uploadPath)) {
    		newFilePath = newFilePath + "/" + subPath + "/";
    	}
		String newFilePathAndName = newFilePath + newFileName;
		//新建存放文件夹
		File f = new File(newFilePath);
		if (!f.exists()) {
			f.mkdirs();
		}
		//存文件
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(newFilePathAndName)));
		out.write(file.getBytes());
		out.flush();
		out.close();
		//返回文件url
		return new BaseResult("1", "成功", relativePath);
	}

    /**
     * 数据库文件上传
     */
    @ApiOperation(value="数据库文件上传", notes="")
    @RequestMapping(value="/dbFile", method={RequestMethod.POST})
    public BaseResult dbFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        return fileUploadService.upload(file, dbSavePath, fileName);
    }

    /**
     * 日志文件上传，不改文件名，用于日志云存储
     */
    @ApiOperation(value="日志文件上传", notes="")
    @RequestMapping(value="/logFile", method={RequestMethod.POST})
    public BaseResult logFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        String savePath = uploadPath + "/logBackup/";
        return fileUploadService.upload(file, savePath, fileName);
    }

    /**
     * 广告图片上传
     */
//	@ApiOperation(value="广告图片上传", notes="")
//    @RequestMapping(value="/uploadAdImage", method={RequestMethod.POST})
//    public BaseResult uploadAdImage(MultipartFile file)
//            throws Exception {
//	    BaseResult result = uploadCommonFile(file, "image");
//	    String relativePath = (String) result.getData();
//	    // 存入数据库
//	    AdImage adImage = new AdImage();
//	    adImage.setName(relativePath);
//	    adImage.setImageUrl(relativePath);
//	    adImage.setIsValid(1);
//	    adImageService.save(adImage);
//	    return new BaseResult(relativePath);
//    }

}
