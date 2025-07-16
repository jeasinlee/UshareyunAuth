package cn.ushare.account.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ExcelUtil {

    /**
     * Excel表格导出
     * @param response HttpServletResponse对象
     * @param head 标题
     * @param excelData Excel表格的数据，封装为List<List<String>>
     * @param sheetName sheet的名字
     * @param fileName 导出Excel的文件名
     * @param columnWidth Excel表格的宽度，建议为15
     * @throws IOException 抛IO异常
     */
    public static void exportExcel(HttpServletResponse response,
                                   List<String> head,
                                   List<List<String>> excelData,
                                   String sheetName,
                                   String fileName,
                                   int columnWidth) throws IOException {

        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称
        HSSFSheet sheet = workbook.createSheet(sheetName);
        //设置表格列宽度
        sheet.setDefaultColumnWidth(columnWidth);

        HSSFFont headFont = workbook.createFont();
        headFont.setFontName("宋体");//名称-宋体
        headFont.setFontHeightInPoints((short)16);//高度-14
        headFont.setColor(HSSFColor.BLACK.index);//颜色
        headFont.setBold(true);//加粗

        HSSFCellStyle headStyle = workbook.createCellStyle();
        headStyle.setFillBackgroundColor(HSSFColor.LIGHT_YELLOW.index);
        headStyle.setFont(headFont);

        HSSFFont rowFont = workbook.createFont();
        rowFont.setFontName("宋体");//名称-宋体
        rowFont.setFontHeightInPoints((short)14);//高度-14
        rowFont.setColor(HSSFColor.GREY_80_PERCENT.index);//颜色

        HSSFDataFormat df = workbook.createDataFormat();

        HSSFRow rowHead = sheet.createRow(0);
        for (int i = 0; i < head.size(); i++) {
            //创建一个单元格
            HSSFCell cell = rowHead.createCell(i);
            //创建一个内容对象
            HSSFRichTextString text = new HSSFRichTextString(head.get(i));
            //将内容对象的文字内容写入到单元格中
            cell.setCellValue(text);
            cell.setCellStyle(headStyle);
        }

        //写入List<List<String>>中的数据
        int rowIndex = 1;

        HSSFCellStyle contextstyle = workbook.createCellStyle();
        contextstyle.setFont(rowFont);
        for(List<String> data : excelData){
            //创建一个row行，然后自增1
            HSSFRow row = sheet.createRow(rowIndex++);
            //遍历添加本行数据
            for (int i = 0; i < data.size(); i++) {
                //创建一个单元格
                HSSFCell cell = row.createCell(i);
                //创建一个内容对象
                HSSFRichTextString text = new HSSFRichTextString(data.get(i));

                Boolean isNum = false;//data是否为数值型
                if (data.get(i) != null || "".equals(data.get(i))) {
                    //判断data是否为数值型
                    isNum = data.get(i).matches("^(-?\\d+)(\\.\\d+)?$");
                }
                if(isNum && (i==4 || i==5)){
                    contextstyle.setDataFormat(df.getBuiltinFormat("#,##0.00"));//保留两位小数点
                    // 设置单元格格式
                    cell.setCellStyle(contextstyle);
                    // 设置单元格内容为double类型
                    cell.setCellValue(Double.parseDouble(data.get(i)));
                }else {
                    cell.setCellStyle(contextstyle);
                    cell.setCellValue(text);
                }
            }
        }

        fileName = new String(fileName.getBytes("utf-8"),"ISO-8859-1" ) + ".xls";

        // 捕获内存缓冲区的数据，转换成字节数组
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        // 获取内存缓冲中的数据
        byte[] content = out.toByteArray();
        // 将字节数组转化为输入流
        InputStream in = new ByteArrayInputStream(content);
        //通过调用reset（）方法可以重新定位。
        response.reset();
        // 如果文件名是英文名不需要加编码格式，如果是中文名需要添加"iso-8859-1"防止乱码
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.addHeader("Content-Length", "" + content.length);
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        ServletOutputStream outputStream = response.getOutputStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        byte[] buff = new byte[8192];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
        }
        bis.close();
        bos.close();
        outputStream.flush();
        outputStream.close();
    }

    public static void main(String[] args) {
        System.out.println("===" + new BigDecimal(0.85).toString());
        System.out.println("===="+new BigDecimal(1550).divide(new BigDecimal(100)).setScale(2, RoundingMode.DOWN).toString());
    }
}
