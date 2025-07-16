package cn.ushare.account.admin.service.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.ushare.account.admin.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.ushare.account.entity.BaseResult;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {
    
    @Override
    public BaseResult upload(MultipartFile file, String savePath, 
            String fileName) {
        // 存放路径及文件名
        String relativePath = "/" + fileName;  
        String newFilePathAndName = savePath + relativePath;  
        // 路径不存在则新建
        File f = new File(savePath);
        if (!f.exists()) {
            f.mkdirs();
        }
        //存文件
        BufferedOutputStream out;
        try {
            out = new BufferedOutputStream(
                    new FileOutputStream(new File(newFilePathAndName)));
            out.write(file.getBytes());  
            out.flush();  
            out.close();
        } catch (FileNotFoundException e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", e.getMessage(), null);
        } catch (IOException e) {
            log.error("Error Exception=", e);
            return new BaseResult("0", e.getMessage(), null);
        }  
          
        return new BaseResult(fileName);
    }
    
    

}
