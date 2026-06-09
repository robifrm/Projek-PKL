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
}