package cn.ushare.account.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream; 

public class ZipUtil {
    
    /**
     * Java无需解压直接读取Zip文件和文件内容，
     * @param file
     * @throws Exception
     */
     public static List<String> readZipFile(String file) throws Exception {  
         ZipFile zf = new ZipFile(file);  
         List<String> strList = new ArrayList<String>();
         InputStream in = new BufferedInputStream(new FileInputStream(file));  
         ZipInputStream zin = new ZipInputStream(in);  
         ZipEntry ze;  
         String str="";
         while ((ze = zin.getNextEntry()) != null) {  
             if (ze.isDirectory()) {
             } else {  
//                 System.err.println("file - " + ze.getName() + " : "  
//                         + ze.getSize() + " bytes");  
                 long size = ze.getSize();  
                 if (size > 0) {  
                     BufferedReader br = new BufferedReader(  
                             new InputStreamReader(zf.getInputStream(ze)));  
                     String line;  
                     while ((line = br.readLine()) != null) {  
                         str+=line;  
                     }  
                     strList.add(str);
                     br.close();  
                 }  
             }  
             str="";
         }  
         zf.close();
         in.close();
         zin.closeEntry();  
         return strList;
     } 

}
