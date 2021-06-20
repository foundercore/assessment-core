package com.assessment.common;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class XLWriter {

    public static void write(File file, List<Map<String, Object>> data) throws IOException {
        /* check input data */
        if (data == null || data.isEmpty()) return;

        /* create target parent directory */
        if (!file.getParentFile().exists()) {
            Files.createParentDirs(file);
        }

        /* start writing xl */
        Workbook workbook = new XSSFWorkbook();
        String sheetName = "data";
        List<String> headers = Arrays.asList(data.get(0).keySet().toArray(new String[0]));

        Sheet sheet = workbook.createSheet(sheetName);
        /* header row */
        int rowCount = 0;
        Row row00 = sheet.createRow(rowCount++);

        /* to enable newlines you need set a cell styles with wrap=true */
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        int columnCount = 0;
        for (String key : headers) {
            Cell keyCell = row00.createCell(columnCount++);
            keyCell.setCellValue(key);
            keyCell.setCellStyle(headerStyle);
        }

        /* body */
        CellStyle bodyStyle = workbook.createCellStyle();
        bodyStyle.setBorderBottom(BorderStyle.THIN);
        bodyStyle.setBorderTop(BorderStyle.THIN);
        bodyStyle.setBorderLeft(BorderStyle.THIN);
        bodyStyle.setBorderRight(BorderStyle.THIN);

        for (Map<String, Object> map: data) {
            columnCount = 0;
            Row dataRow = sheet.createRow(rowCount++);
            for (String key: headers) {
                Cell dataCell = dataRow.createCell(columnCount++);
                dataCell.setCellValue(String.valueOf(map.get(key) == null? "": map.get(key)));
                dataCell.setCellStyle(bodyStyle);
            }
        }

        /* Auto-size columns */
        int index = 0;
        for (String key: headers) {
            sheet.autoSizeColumn(index);
            index++;
        }

        /* write workbook into file */
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.flush();
        }finally {
            if (fileOut != null){
                try {
                    fileOut.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
