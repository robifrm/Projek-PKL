package com.vnet.vnet_backend.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
public class ExcelService {

    public List<Map<String, String>> parseFile(MultipartFile file) {
        List<Map<String, String>> result = new ArrayList<>();

        try (
                InputStream inputStream = file.getInputStream();
                // Gunakan WorkbookFactory agar support .xls dan .xlsx sekaligus
                Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new RuntimeException("Header Excel tidak ditemukan.");
            }

            // Optimasi performa: Buat instansiasi DataFormatter cukup 1x saja di luar loop
            DataFormatter dataFormatter = new DataFormatter();

            // Simpan header yang valid (skip header kosong)
            List<String> headers = new ArrayList<>();
            int lastCellNum = headerRow.getLastCellNum();

            for (int j = 0; j < lastCellNum; j++) {
                Cell cell = headerRow.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String header = dataFormatter.formatCellValue(cell).trim();

                if (header.isEmpty()) {
                    headers.add(null); // penanda untuk skip kolom ini
                } else {
                    headers.add(header);
                }
            }

            // Loop seluruh data
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                Map<String, String> rowData = new LinkedHashMap<>();
                boolean isRowEmpty = true;

                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);

                    // Skip kolom yang header-nya kosong
                    if (header == null) {
                        continue;
                    }

                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = dataFormatter.formatCellValue(cell).trim();

                    if (!value.isEmpty()) {
                        isRowEmpty = false;
                    }

                    rowData.put(header, value);
                }

                // Jangan masukkan row kosong
                if (!isRowEmpty) {
                    result.add(rowData);
                }
            }

        } catch (Exception e) {
            // Cetak root cause di console backend agar mempermudah debug
            e.printStackTrace();
            throw new RuntimeException("Gagal membaca file Excel: " + e.getMessage(), e);
        }

        return result;
    }

    public byte[] generateTemplate() {
        try (
                Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("Template Import");
            
            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            
            // Headers
            String[] headers = {
                "No", "Tanggal Registrasi", "Agent", "Nama", "Alamat", 
                "RT/RW", "Kelurahan / Desa", "Kecamatan", "Kota/Kabupaten", 
                "Kode Pos", "Nomor Telepon", "Email", "Package", "Status", 
                "Tanggal Aktivasi", "Cust ID", "Price", "Profit",
                "Biaya Pasang", "Isolir"
            };
            
            Row headerRow = sheet.createRow(0);
            for (int j = 0; j < headers.length; j++) {
                Cell cell = headerRow.createCell(j);
                cell.setCellValue(headers[j]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add a sample row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("1");
            sampleRow.createCell(1).setCellValue("17/06/2026");
            sampleRow.createCell(2).setCellValue("Pt. Zanara");
            sampleRow.createCell(3).setCellValue("Robi");
            sampleRow.createCell(4).setCellValue("Jl. Sudirman No. 10");
            sampleRow.createCell(5).setCellValue("01/02");
            sampleRow.createCell(6).setCellValue("Cikole");
            sampleRow.createCell(7).setCellValue("Cikole");
            sampleRow.createCell(8).setCellValue("Sukabumi");
            sampleRow.createCell(9).setCellValue("43110");
            sampleRow.createCell(10).setCellValue("081234567890");
            sampleRow.createCell(11).setCellValue("robi@gmail.com");
            sampleRow.createCell(12).setCellValue("100 Mbps");
            sampleRow.createCell(13).setCellValue("Active");
            sampleRow.createCell(14).setCellValue("17/06/2026");
            sampleRow.createCell(15).setCellValue("VN12345");
            sampleRow.createCell(16).setCellValue(500000.0);
            sampleRow.createCell(17).setCellValue(150000.0);
            sampleRow.createCell(18).setCellValue(200000.0);
            sampleRow.createCell(19).setCellValue(false);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Gagal generate template Excel: " + e.getMessage(), e);
        }
    }
}