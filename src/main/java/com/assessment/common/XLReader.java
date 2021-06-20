package com.assessment.common;

import com.monitorjbl.xlsx.StreamingReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class XLReader implements Iterator, Closeable, IFileDecoder {

    int rowCount = 0;
    private File inputFile;
    private boolean fsClosed = false;
    private Boolean isXlsx = null;
    private List<String> headers = new LinkedList<>();
    private Iterator rows;
    private FormulaEvaluator evaluator;
    private Workbook wb;

    private LinkedHashMap<String, Object> currentRow = new LinkedHashMap<>();

    public XLReader(String fileName) throws IOException {
        this(fileName, 0, true, false);
    }


    public XLReader(String fileName, int sheetNo, boolean fileHasHeader, boolean columnNameToLowerCase) throws IOException {
        try {
            if ("xlsx".equalsIgnoreCase(getExtension(fileName))) {
                InputStream is = new FileInputStream(new File(fileName));
                wb = StreamingReader
                        .builder()
                        .rowCacheSize(100)
                        .bufferSize(4096)
                        .open(is);
                Sheet sheet = wb.getSheetAt(sheetNo);
                rows = sheet.rowIterator();
                isXlsx = true;
            } else if ("xls".equalsIgnoreCase(getExtension(fileName))) {
                inputFile = new File(fileName);
                wb = WorkbookFactory.create(new FileInputStream(fileName));
                HSSFSheet sheet = (HSSFSheet) wb.getSheetAt(sheetNo);
                rows = sheet.rowIterator();
                evaluator = wb.getCreationHelper().createFormulaEvaluator();
                isXlsx = false;
            } else {
                throw new UnsupportedOperationException("Invalid file type : " + getExtension(fileName));
            }
        } catch (Exception e) {
            throw new IOException("Error loading file. Sheet - " + sheetNo + ", file - " + fileName + ", Error - " + e.getMessage());
        }
        init(fileHasHeader, columnNameToLowerCase);
    }

    private void init(boolean fileHasHeader, boolean columnNameToLowerCase) {
        if (fileHasHeader) {

            if (rows.hasNext()) {

                rowCount++;

                Iterator cells;
                if (isXlsx) {
                    Row row = (Row) rows.next();
                    cells = row.cellIterator();
                } else {
                    HSSFRow row = (HSSFRow) rows.next();
                    cells = row.cellIterator();
                }

                for (Object obj : processCellValues(cells)) {
                    String col = (String) obj;
                    if (columnNameToLowerCase) {
                        headers.add(col.toLowerCase());
                    } else {
                        headers.add(col);
                    }
                }
            }
        }
    }

    @Override
    public boolean hasNext() {

        if (rows.hasNext()) {
            currentRow = new LinkedHashMap<>();
            rowCount++;
            int maxCellCount;
            Row pRow;
            if (isXlsx) {
                Row row = (Row) rows.next();
                maxCellCount = row.getLastCellNum();
                pRow = row;
            } else {
                HSSFRow row = (HSSFRow) rows.next();
                maxCellCount = row.getLastCellNum();
                pRow = row;
            }
            if (fileHasHeader()) {
                maxCellCount = Math.max(headers.size(), maxCellCount);
            }

            for (int idx = 0; idx < maxCellCount; idx++) {
                Cell cell = pRow.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Object value = null;
                if (cell != null) {
                    if (isXlsx) {
                        Cell xCell = (Cell) cell;
                        value = getCellValueForXlsx(xCell, idx + 1);
                    } else {
                        HSSFCell hCell = (HSSFCell) cell;
                        value = getCellValueForXls(hCell, idx + 1);
                    }
                }
                if (fileHasHeader()) {
                    currentRow.put(headers.get(idx), value);
                } else {
                    currentRow.put(String.valueOf(idx), value);
                }
            }
            return true;
        } else {
            currentRow = null;
            close();
            return false;
        }
    }

    @Override
    public LinkedHashMap<String, Object> next() {

        if (currentRow == null) {
            return new LinkedHashMap<>();
        }
        return currentRow;
    }

    private boolean fileHasHeader() {
        return headers.size() != 0;
    }

    public List<String> getHeaders() {
        return headers;
    }

    private List<Object> processCellValues(Iterator cells) {

        int cellCount = 0;
        List<Object> columnValueList = new LinkedList<>();

        while (cells.hasNext()) {

            cellCount++;
            if (isXlsx) {
                Cell cell = (Cell) cells.next();
                columnValueList.add(getCellValueForXlsx(cell, cellCount));
            } else {
                HSSFCell cell = (HSSFCell) cells.next();
                columnValueList.add(getCellValueForXls(cell, cellCount));
            }
        }

        return columnValueList;
    }

    private Object getCellValueForXlsx(Cell cell, int cellCount) {

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();

                }
            case BLANK:
                return null;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                // default exception to be thrown in case of error
            case FORMULA:
                // no break applied as it should be handles as the switch statement already evaluate
                if (CellType.NUMERIC.equals(cell.getCachedFormulaResultType())) {
                    return cell.getNumericCellValue();
                } else if (CellType.STRING.equals(cell.getCachedFormulaResultType())) {
                    try{
                        return cell.getRichStringCellValue().getString();
                    }catch (Exception ignored){}
                    return cell.getStringCellValue();
                } else if (CellType.BOOLEAN.equals(cell.getCachedFormulaResultType())) {
                    return cell.getBooleanCellValue();
                } else {
                    return null;
                }
            default:
                throw new UnsupportedOperationException("Cannot process cell type : " + cell.getCellType() +
                        " at row : " + rowCount + " | col : " + cellCount);
        }
    }

    private Object getCellValueForXls(HSSFCell cell, int cellCount) {

        switch (evaluator.evaluateInCell(cell).getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue();
                else return cell.getNumericCellValue();
            case BLANK:
                return null;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                // default exception to be thrown in case of error
            case FORMULA:
                // no break applied as it should be handles as the switch statement already evaluate
                if (CellType.NUMERIC.equals(cell.getCachedFormulaResultType())) {
                    return cell.getNumericCellValue();
                } else if (CellType.STRING.equals(cell.getCachedFormulaResultType())) {
                    try{
                        return cell.getRichStringCellValue().getString();
                    }catch (Exception ignored){}
                    return cell.getStringCellValue();
                } else if (CellType.BOOLEAN.equals(cell.getCachedFormulaResultType())) {
                    return cell.getBooleanCellValue();
                } else {
                    return null;
                }
            default:
                throw new UnsupportedOperationException("Cannot process cell type : " + cell.getCellType() +
                        " at row : " + rowCount + " | col : " + cellCount);
        }
    }

    private String getExtension(String fileName) {

        String[] splitArr = fileName.split("\\.");
        if (splitArr.length > 1) {
            return splitArr[splitArr.length - 1];
        }
        return fileName;
    }

    @Override
    public void close() {
        try {
            if (wb != null) wb.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
