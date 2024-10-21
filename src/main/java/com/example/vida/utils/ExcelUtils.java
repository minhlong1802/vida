package com.example.vida.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelUtils {

    public static Workbook readExcelFile(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return new XSSFWorkbook(fis);
        }
    }

    public static void writeExcelFile(Workbook workbook, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
    }

    public static Sheet readSheet(Workbook workbook, String sheetName) {
        return workbook.getSheet(sheetName);
    }

    public static void writeSheet(Workbook workbook, String sheetName, List<List<String>> data) {
        Sheet sheet = workbook.createSheet(sheetName);
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i);
            List<String> rowData = data.get(i);
            for (int j = 0; j < rowData.size(); j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(rowData.get(j));
            }
        }
    }

    public static void updateCell(Sheet sheet, int rowIndex, int colIndex, String newValue) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellValue(newValue);
    }

    public static void deleteColumn(Sheet sheet, int colIndex) {
        for (Row row : sheet) {
            Cell cell = row.getCell(colIndex);
            if (cell != null) {
                row.removeCell(cell);
            }
        }
    }

    public static void formatCell(Sheet sheet, int rowIndex, int colIndex, CellStyle style) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        cell.setCellStyle(style);
    }
}