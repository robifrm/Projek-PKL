package com.vnet.vnet_backend.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.vnet.vnet_backend.entity.Customer;
import java.time.format.DateTimeFormatter;

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
                "No", "Tgl Aktivasi", "Cust ID", "Nama", "Alamat", "Bandwidth",
                "Panjang Kabel (Meter)", "Koordinat Rumah", "Kode ODP", "Port ODP",
                "Port ODP Terpakai", "POP", "OLT", "SN ONT", "Rosset", "Pigtail",
                "Patchcore", "Splicing", "Redaman Output Kabel", "Jumlah Bracket/Spiral",
                "Jumlah S-Clamp", "Status Aktivasi", "Teknisi 1", "Equipment"
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
            sampleRow.createCell(2).setCellValue("VN33021");
            sampleRow.createCell(3).setCellValue("Robi");
            sampleRow.createCell(4).setCellValue("Jl. Sudirman No. 10, RT/RW 01/02, Cikole, Cikole, Sukabumi 43110");
            sampleRow.createCell(5).setCellValue("100 Mbps");
            sampleRow.createCell(6).setCellValue(150.0);
            sampleRow.createCell(7).setCellValue("-6.9214, 106.9284");
            sampleRow.createCell(8).setCellValue("ODP-CIK-01");
            sampleRow.createCell(9).setCellValue(16.0);
            sampleRow.createCell(10).setCellValue("3");
            sampleRow.createCell(11).setCellValue("POP-CIK");
            sampleRow.createCell(12).setCellValue("OLT-ZTE-01");
            sampleRow.createCell(13).setCellValue("ZTEG12345678");
            sampleRow.createCell(14).setCellValue("Ya");
            sampleRow.createCell(15).setCellValue(2.0);
            sampleRow.createCell(16).setCellValue(1.0);
            sampleRow.createCell(17).setCellValue(2.0);
            sampleRow.createCell(18).setCellValue("-18.5");
            sampleRow.createCell(19).setCellValue(3.0);
            sampleRow.createCell(20).setCellValue(5.0);
            sampleRow.createCell(21).setCellValue("Done");
            sampleRow.createCell(22).setCellValue("Teknisi Lapangan 1");
            sampleRow.createCell(23).setCellValue("ONT: 1, Kabel: 150m, Roset: 1");

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

    public byte[] generateCustomerExport(
            List<Customer> customers,
            Map<String, com.vnet.vnet_backend.entity.BaAktivasi> baMap
    ) {
        try (
                Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()
        ) {
            // ==========================================
            // SHEET 1: REKAP IKR GELO
            // ==========================================
            Sheet sheet1 = workbook.createSheet("REKAP IKR GELO");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Row and cell styles
            CellStyle normalStyle = workbook.createCellStyle();
            normalStyle.setVerticalAlignment(VerticalAlignment.TOP);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            String[] headers1 = {
                "No", "Tgl Aktivasi", "Cust ID", "Nama", "Alamat", "Bandwidth",
                "Panjang Kabel (Meter)", "Koordinat Rumah", "Kode ODP", "Port ODP",
                "Port ODP Terpakai", "POP", "OLT", "SN ONT", "Rosset", "Pigtail",
                "Patchcore", "Splicing", "Redaman Output Kabel", "Jumlah Bracket/Spiral",
                "Jumlah S-Clamp", "Status Aktivasi", "Teknisi 1", "Equipment"
            };

            Row headerRow1 = sheet1.createRow(0);
            for (int j = 0; j < headers1.length; j++) {
                Cell cell = headerRow1.createCell(j);
                cell.setCellValue(headers1[j]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            int rowIdx1 = 1;
            for (Customer c : customers) {
                Row row = sheet1.createRow(rowIdx1);
                com.vnet.vnet_backend.entity.BaAktivasi ba = baMap.get(c.getCustId());

                // Set cell styles to normal/wrap
                for (int j = 0; j < headers1.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(normalStyle);
                }

                row.getCell(0).setCellValue(rowIdx1);

                String activationDate = "-";
                if (c.getTanggalAktivasi() != null) {
                    activationDate = c.getTanggalAktivasi().format(dtf);
                }
                row.getCell(1).setCellValue(activationDate);

                row.getCell(2).setCellValue(c.getCustId() != null ? c.getCustId() : "-");
                row.getCell(3).setCellValue(c.getNama() != null ? c.getNama() : "-");

                // Full Address
                StringBuilder addr = new StringBuilder();
                if (c.getAddress() != null) {
                    if (c.getAddress().getAlamat() != null) addr.append(c.getAddress().getAlamat());
                    if (c.getAddress().getRtRw() != null && !c.getAddress().getRtRw().trim().isEmpty()) {
                        addr.append(", RT/RW ").append(c.getAddress().getRtRw());
                    }
                    if (c.getAddress().getKelurahan() != null) addr.append(", ").append(c.getAddress().getKelurahan());
                    if (c.getAddress().getKecamatan() != null) addr.append(", ").append(c.getAddress().getKecamatan());
                    if (c.getAddress().getKota() != null) addr.append(", ").append(c.getAddress().getKota());
                    if (c.getAddress().getKodePos() != null && !c.getAddress().getKodePos().trim().isEmpty()) {
                        addr.append(" ").append(c.getAddress().getKodePos());
                    }
                }
                row.getCell(4).setCellValue(addr.length() > 0 ? addr.toString() : "-");
                row.getCell(4).setCellStyle(wrapStyle);

                // Bandwidth
                String bw = "-";
                if (ba != null && ba.getBandwidth() != null && !ba.getBandwidth().trim().isEmpty()) {
                    bw = ba.getBandwidth();
                } else if (c.getPkg() != null) {
                    bw = c.getPkg().getName() + " (" + (c.getPkg().getSpeed() != null ? c.getPkg().getSpeed() : "0") + " Mbps)";
                }
                row.getCell(5).setCellValue(bw);

                // Panjang Kabel (Meter)
                Integer pk = 0;
                if (ba != null) {
                    pk = ba.getPanjangKabel() != null ? ba.getPanjangKabel() : (ba.getQtyKabel() != null ? ba.getQtyKabel() : 0);
                }
                row.getCell(6).setCellValue(pk);

                // Koordinat Rumah
                String kr = "-";
                if (ba != null && ba.getKoordinatRumah() != null && !ba.getKoordinatRumah().trim().isEmpty()) {
                    kr = ba.getKoordinatRumah();
                } else if (c.getKoordinat() != null) {
                    kr = c.getKoordinat();
                }
                row.getCell(7).setCellValue(kr);

                // Kode ODP
                row.getCell(8).setCellValue((ba != null && ba.getKodeOdp() != null) ? ba.getKodeOdp() : "-");

                // Port ODP
                row.getCell(9).setCellValue((ba != null && ba.getPortOdp() != null) ? ba.getPortOdp().toString() : "-");

                // Port ODP Terpakai
                row.getCell(10).setCellValue((ba != null && ba.getPortOdpTerpakai() != null) ? ba.getPortOdpTerpakai() : "-");

                // POP
                row.getCell(11).setCellValue((ba != null && ba.getPop() != null) ? ba.getPop() : "-");

                // OLT
                row.getCell(12).setCellValue((ba != null && ba.getOlt() != null) ? ba.getOlt() : "-");

                // SN ONT
                row.getCell(13).setCellValue((ba != null && ba.getSnOnt() != null) ? ba.getSnOnt() : "-");

                // Rosset
                String rs = "-";
                if (ba != null) {
                    if (ba.getRosset() != null && !ba.getRosset().trim().isEmpty()) {
                        rs = ba.getRosset();
                    } else if (ba.getQtyRoset() != null && ba.getQtyRoset() > 0) {
                        rs = "Ya";
                    } else {
                        rs = "Tidak";
                    }
                }
                row.getCell(14).setCellValue(rs);

                // Pigtail
                row.getCell(15).setCellValue((ba != null && ba.getPigtail() != null) ? ba.getPigtail() : 0);

                // Patchcore
                row.getCell(16).setCellValue((ba != null && ba.getPatchcore() != null) ? ba.getPatchcore() : 0);

                // Splicing
                row.getCell(17).setCellValue((ba != null && ba.getSplicing() != null) ? ba.getSplicing() : 0);

                // Redaman Output Kabel
                row.getCell(18).setCellValue((ba != null && ba.getRedamanOutputKabel() != null) ? ba.getRedamanOutputKabel() : "-");

                // Jumlah Bracket/Spiral
                row.getCell(19).setCellValue((ba != null && ba.getJumlahBracket() != null) ? ba.getJumlahBracket() : 0);

                // Jumlah S-Clamp
                row.getCell(20).setCellValue((ba != null && ba.getJumlahSClamp() != null) ? ba.getJumlahSClamp() : 0);

                // Status Aktivasi
                row.getCell(21).setCellValue((ba != null && ba.getStatusAktivasi() != null) ? ba.getStatusAktivasi() : "Done");

                // Teknisi 1
                row.getCell(22).setCellValue((ba != null && ba.getNamaTeknisi() != null) ? ba.getNamaTeknisi() : "-");

                // Equipment (Bulleted list going downwards)
                String eqStr = "-";
                if (ba != null) {
                    List<String> eqList = new ArrayList<>();
                    if (ba.getQtyOnt() != null && ba.getQtyOnt() > 0) {
                        eqList.add("- ONT: " + ba.getQtyOnt() + " (SN: " + (ba.getSnOnt() != null ? ba.getSnOnt() : "-") + ")");
                    }
                    if (ba.getQtyRouter() != null && ba.getQtyRouter() > 0) {
                        eqList.add("- Router: " + ba.getQtyRouter() + " (SN: " + (ba.getSnRouter() != null ? ba.getSnRouter() : "-") + ")");
                    }
                    if (ba.getQtyKabel() != null && ba.getQtyKabel() > 0) {
                        eqList.add("- Kabel: " + ba.getQtyKabel() + "m");
                    }
                    if (ba.getQtyRoset() != null && ba.getQtyRoset() > 0) {
                        eqList.add("- Roset: " + ba.getQtyRoset());
                    }
                    if (ba.getQtyAksesoris() != null && ba.getQtyAksesoris() > 0) {
                        eqList.add("- Aksesoris: " + ba.getQtyAksesoris());
                    }
                    if (!eqList.isEmpty()) {
                        eqStr = String.join("\n", eqList);
                    }
                }
                row.getCell(23).setCellValue(eqStr);
                row.getCell(23).setCellStyle(wrapStyle);

                rowIdx1++;
            }

            for (int i = 0; i < headers1.length; i++) {
                sheet1.autoSizeColumn(i);
            }

            // ==========================================
            // SHEET 2: DOKUMENTASI FOTO
            // ==========================================
            Sheet sheet2 = workbook.createSheet("DOKUMENTASI FOTO");

            String[] headers2 = {
                "NO", "Tgl Aktivasi", "Cust ID", "Nama", "Rumah Customer", "ODP Depan",
                "Redaman ODP", "Dalem ODP", "Redaman Kabel", "Depan ONT", "Belakang ONT", "Speed Test"
            };

            Row headerRow2 = sheet2.createRow(0);
            for (int j = 0; j < headers2.length; j++) {
                Cell cell = headerRow2.createCell(j);
                cell.setCellValue(headers2[j]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx2 = 1;
            Drawing<?> drawing2 = sheet2.createDrawingPatriarch();
            for (Customer c : customers) {
                Row row = sheet2.createRow(rowIdx2);
                com.vnet.vnet_backend.entity.BaAktivasi ba = baMap.get(c.getCustId());

                // Set row height to 100pt to accommodate images nicely
                row.setHeightInPoints(100);

                // Set cell styles to normal
                for (int j = 0; j < headers2.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(normalStyle);
                }

                row.getCell(0).setCellValue(rowIdx2);

                String activationDate = "-";
                if (c.getTanggalAktivasi() != null) {
                    activationDate = c.getTanggalAktivasi().format(dtf);
                }
                row.getCell(1).setCellValue(activationDate);

                row.getCell(2).setCellValue(c.getCustId() != null ? c.getCustId() : "-");
                row.getCell(3).setCellValue(c.getNama() != null ? c.getNama() : "-");

                if (ba != null) {
                    insertImageToCell(workbook, sheet2, drawing2, 4, rowIdx2, ba.getFotoRumah());
                    insertImageToCell(workbook, sheet2, drawing2, 5, rowIdx2, ba.getFotoOdpDepan());
                    insertImageToCell(workbook, sheet2, drawing2, 6, rowIdx2, ba.getFotoRedamanOdp());
                    insertImageToCell(workbook, sheet2, drawing2, 7, rowIdx2, ba.getFotoDalemOdp());
                    insertImageToCell(workbook, sheet2, drawing2, 8, rowIdx2, ba.getFotoRedamanKabel());
                    insertImageToCell(workbook, sheet2, drawing2, 9, rowIdx2, ba.getFotoDepanOnt());
                    insertImageToCell(workbook, sheet2, drawing2, 10, rowIdx2, ba.getFotoBelakangOnt());
                    insertImageToCell(workbook, sheet2, drawing2, 11, rowIdx2, ba.getFotoSpeedTest());
                } else {
                    for (int col = 4; col <= 11; col++) {
                        row.getCell(col).setCellValue("-");
                    }
                }

                rowIdx2++;
            }

            // Auto-size text columns
            for (int i = 0; i < 4; i++) {
                sheet2.autoSizeColumn(i);
            }
            // Set image columns to fixed width
            for (int i = 4; i <= 11; i++) {
                sheet2.setColumnWidth(i, 20 * 256);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Gagal generate export Excel customer: " + e.getMessage(), e);
        }
    }

    public byte[] generateRegistrationExport(
            List<com.vnet.vnet_backend.entity.CustomerRegistration> registrations,
            Map<Long, com.vnet.vnet_backend.entity.BaAktivasi> baMap
    ) {
        try (
                Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("Selesai Registrasi");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                "No", "Customer Code", "Nama Customer", "No HP", "Email",
                "Kecepatan Internet (Mbps)", "Alamat", "Equipment", "Harga",
                "Nama Teknisi", "Jadwal Aktivasi"
            };

            Row headerRow = sheet.createRow(0);
            for (int j = 0; j < headers.length; j++) {
                Cell cell = headerRow.createCell(j);
                cell.setCellValue(headers[j]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            int rowIdx = 1;
            for (com.vnet.vnet_backend.entity.CustomerRegistration reg : registrations) {
                Row row = sheet.createRow(rowIdx);

                row.createCell(0).setCellValue(rowIdx);
                row.createCell(1).setCellValue(reg.getCustId() != null ? reg.getCustId() : "-");
                row.createCell(2).setCellValue(reg.getFirstName() + " " + reg.getLastName());
                row.createCell(3).setCellValue(reg.getNomorSelulerUtama() != null ? reg.getNomorSelulerUtama() : "-");
                row.createCell(4).setCellValue(reg.getEmail() != null ? reg.getEmail() : "-");

                String speedStr = "-";
                if (reg.getPkg() != null) {
                    speedStr = reg.getPkg().getName() + " (" + (reg.getPkg().getSpeed() != null ? reg.getPkg().getSpeed() : "0") + " Mbps)";
                }
                row.createCell(5).setCellValue(speedStr);

                // Full Address
                StringBuilder addr = new StringBuilder();
                if (reg.getAlamatDetail() != null) addr.append(reg.getAlamatDetail());
                if (reg.getRt() != null || reg.getRw() != null) {
                    addr.append(", RT ").append(reg.getRt() != null ? reg.getRt() : "-")
                        .append(" / RW ").append(reg.getRw() != null ? reg.getRw() : "-");
                }
                if (reg.getKelurahan() != null) addr.append(", ").append(reg.getKelurahan());
                if (reg.getKecamatan() != null) addr.append(", ").append(reg.getKecamatan());
                if (reg.getKota() != null) addr.append(", ").append(reg.getKota());
                if (reg.getProvinsi() != null) addr.append(", ").append(reg.getProvinsi());
                if (reg.getKodePos() != null) addr.append(" ").append(reg.getKodePos());
                row.createCell(6).setCellValue(addr.toString());

                // Equipment & Teknisi from BA
                String eqStr = "-";
                String techName = "-";
                com.vnet.vnet_backend.entity.BaAktivasi ba = baMap.get(reg.getId());
                if (ba != null) {
                    techName = ba.getNamaTeknisi() != null ? ba.getNamaTeknisi() : "-";
                    List<String> eqList = new ArrayList<>();
                    if (ba.getQtyOnt() != null && ba.getQtyOnt() > 0) {
                        eqList.add("Modem / ONT / ONU (SN: " + (ba.getSnOnt() != null ? ba.getSnOnt() : "-") + ") Qty " + ba.getQtyOnt());
                    }
                    if (ba.getQtyRouter() != null && ba.getQtyRouter() > 0) {
                        eqList.add("Router (SN: " + (ba.getSnRouter() != null ? ba.getSnRouter() : "-") + ") Qty " + ba.getQtyRouter());
                    }
                    if (ba.getQtyKabel() != null && ba.getQtyKabel() > 0) {
                        eqList.add("Drop Core " + ba.getQtyKabel() + " Meter");
                    }
                    if (ba.getQtyRoset() != null && ba.getQtyRoset() > 0) {
                        eqList.add("Roset Qty " + ba.getQtyRoset());
                    }
                    if (ba.getQtyAksesoris() != null && ba.getQtyAksesoris() > 0) {
                        eqList.add("Aksesoris Qty " + ba.getQtyAksesoris());
                    }
                    if (!eqList.isEmpty()) {
                        eqStr = String.join(", ", eqList);
                    }
                }
                row.createCell(7).setCellValue(eqStr);

                double price = (reg.getPkg() != null && reg.getPkg().getPrice() != null) ? reg.getPkg().getPrice() : 0.0;
                row.createCell(8).setCellValue(price);
                row.createCell(9).setCellValue(techName);

                String jadwalStr = reg.getTanggalJadwal() != null ? reg.getTanggalJadwal().format(dtf) : "-";
                row.createCell(10).setCellValue(jadwalStr);

                rowIdx++;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Gagal generate export Excel registrasi: " + e.getMessage(), e);
        }
    }

    private void insertImageToCell(Workbook workbook, Sheet sheet, Drawing<?> drawing, int col, int rowIdx, String base64Data) {
        Cell cell = sheet.getRow(rowIdx).getCell(col);
        if (base64Data == null || base64Data.trim().isEmpty() || !base64Data.contains("base64,")) {
            cell.setCellValue("-");
            return;
        }
        try {
            String[] parts = base64Data.split("base64,");
            if (parts.length < 2) {
                cell.setCellValue("-");
                return;
            }
            byte[] bytes = java.util.Base64.getDecoder().decode(parts[1].trim());
            
            int pictureType = Workbook.PICTURE_TYPE_PNG;
            if (base64Data.contains("image/jpeg") || base64Data.contains("image/jpg")) {
                pictureType = Workbook.PICTURE_TYPE_JPEG;
            }
            
            int pictureIdx = workbook.addPicture(bytes, pictureType);
            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            
            anchor.setCol1(col);
            anchor.setRow1(rowIdx);
            anchor.setCol2(col + 1);
            anchor.setRow2(rowIdx + 1);
            
            drawing.createPicture(anchor, pictureIdx);
            cell.setCellValue("");
        } catch (Exception e) {
            System.err.println("Failed to insert image to Excel cell (col=" + col + ", row=" + rowIdx + "): " + e.getMessage());
            cell.setCellValue("[Error Gambar]");
        }
    }
}