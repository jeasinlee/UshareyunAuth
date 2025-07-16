package cn.ushare.account.admin.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Api(tags = "FileDownloadController", description = "文件下载")
@RestController
@Slf4j
@RequestMapping("/fileDownload")
public class FileDownloadController {

    @Value("${path.dbSavePath}")
    String dbSavePath;
    @Autowired
    HttpServletResponse response;

    @RequestMapping(value="/downloadDbFile", method={RequestMethod.GET})
    public void fileDownload(String fileName) {
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);

        ServletOutputStream out = null;
        FileInputStream inputStream = null;
        File file = new File(dbSavePath + "/" + fileName);
        try {
            inputStream = new FileInputStream(file);
            out = response.getOutputStream();
            int b = 0;
            byte[] buffer = new byte[512];
            while (b != -1) {
                b = inputStream.read(buffer);
                if (b != -1) {
                    out.write(buffer, 0, b);
                }
            }
        } catch (IOException e) {
            log.error("Error Exception=", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                    out.flush();
                }
            } catch (IOException e) {
                log.error("Error Exception=", e);
            }
        }
    }
}
