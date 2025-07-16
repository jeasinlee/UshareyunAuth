package cn.ushare.account.util;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
  
/** 
 * 基于Ant的Zip压缩工具类，可处理压缩包中的中文文件名
 */  
public class ZipUtilAnt {  
  
    //不能使用utf-8，因为如果压缩包中有中文名文件，getInputStream(zipEntry)会为空
    public static final String ENCODING_DEFAULT = "GB2312";
  
    public static final int BUFFER_SIZE_DIFAULT = 128;  
  
    public static List<FileObj> readZipFileWithName(String file) throws Exception {  
        ZipFile zip = new ZipFile(file, ENCODING_DEFAULT);
        List<FileObj> fileList = new ArrayList<FileObj>();
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.getEntries();    
        String str="";
        while (entries.hasMoreElements()) {    
            ZipEntry zipEntry = entries.nextElement();    
            if (zipEntry.isDirectory()) {    
                // TODO    
            } else {    
                FileObj fileObj = new FileObj();
                    fileObj.name = zipEntry.getName();
                InputStream is = zip.getInputStream(zipEntry);    
                byte[] buff = new byte[BUFFER_SIZE_DIFAULT];    
                int size;    
                while ((size = is.read(buff, 0, BUFFER_SIZE_DIFAULT)) > 0) {
                    if (size == BUFFER_SIZE_DIFAULT) {
                        str += new String(buff); 
                    } else {//读到文件末尾时，buff中会填充0，转换成String后，xml无法解析，要去掉
                        byte[] tempBuf = new byte[size];
                        System.arraycopy(buff, 0, tempBuf, 0, size);
                        str += new String(tempBuf);
                    }                      
                }    
                fileObj.str = str;
                fileList.add(fileObj);
                is.close();    
            }
            str="";
        }   
        return fileList;
    } 
     
    public static class FileObj {
        public String str;
        public String name;
    }
  
     
}
