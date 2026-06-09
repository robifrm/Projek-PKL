# Maintenance, User Manual & Troubleshooting Guide - VNet

Dokumen ini ditulis sebagai panduan operasional harian bagi administrator sistem, tim IT support, dan DevOps dalam memelihara stabilitas platform VNet pasca-rilis.

---

## Bagian I: Panduan Penggunaan Admin (Admin User Manual)

### 1. Membaca Dashboard & Analytics
Dashboard utama VNet menyediakan visualisasi data real-time:
*   **Total Pelanggan Aktif:** Menampilkan jumlah pelanggan dengan status `ACTIVE`. Angka ini menentukan estimasi pendapatan bulanan.
*   **Rasio Churn (Isolir Rate):** Persentase pelanggan yang berstatus `ISOLIR` dibandingkan total seluruh pelanggan. Jika angka ini naik di atas 10%, tim billing disarankan untuk melakukan penagihan aktif.
*   **Grafik Pendapatan Bulanan:** Estimasi kasar omset kotor bulanan dihitung dari: `SUM(price of package for all ACTIVE customers)`.

### 2. Siklus Status Pelanggan (Customer Lifecycle)
*   **Registrasi Pelanggan Baru:** Didata oleh admin atau diajukan oleh agen. Status default saat pendaftaran adalah `ACTIVE`.
*   **Isolir Otomatis/Manual:** Jika pelanggan terlambat membayar melewati jatuh tempo, ubah status pelanggan ke `ISOLIR` melalui tabel pelanggan. Tindakan ini memutus akses internet sementara dan secara otomatis memperbarui grafik metrik pendapatan di dashboard.
*   **Terminasi (`INACTIVE`):** Jika pelanggan memutuskan untuk berhenti berlangganan, ubah status ke `INACTIVE`. Pelanggan berstatus `INACTIVE` tidak akan dihitung ke dalam estimasi pendapatan bulanan.

---

## Bagian II: Monitoring Logs (Pemantauan Log Sistem)

Membaca log adalah langkah pertama untuk mendeteksi anomali/error.

### 1. Log Server Backend (Spring Boot)
Untuk memantau log backend secara real-time pada server Linux:
```bash
sudo journalctl -u vnet-backend.service -n 100 -f
```
Atau jika dijalankan manual, arahkan output log ke file dan baca menggunakan `tail`:
```bash
tail -f /var/log/vnet/backend.log
```

### 2. Log Nginx Web Server
*   **Log Error (Sangat berguna jika frontend blank atau muncul error 502/504):**
    ```bash
    tail -f /var/log/nginx/error.log
    ```
*   **Log Akses (Memantau statistik hit request API dari pengguna):**
    ```bash
    tail -f /var/log/nginx/access.log
    ```

---

## Bagian III: Troubleshooting (Penanganan Masalah Umum)

### 1. Error: "Failed to send email via Resend"
*   **Gejala:** Pada log backend muncul pesan warning: `[EMAIL FALLBACK] Gagal kirim email ke ... Error: Failed to send email via Resend`. Pengguna tidak mendapatkan email berisi kode OTP registrasi.
*   **Penyebab:** API Key Resend salah, kedaluwarsa, atau limit kuota akun Resend gratisan telah habis.
*   **Solusi:**
    1.  Cek log backend untuk melihat kode OTP yang dicetak secara aman (misal: `OTP = 391467`). Admin dapat membisikkan kode ini kepada user secara manual sebagai alternatif cepat.
    2.  Periksa dan ganti nilai `RESEND_API_KEY` di environment variables backend dengan API Key yang aktif.
    3.  Restart service backend: `sudo systemctl restart vnet-backend.service`.

### 2. Error: "Field 'username' doesn't have a default value" (Database Error)
*   **Gejala:** Saat pengguna mendaftar, respon API mengembalikan `400 Bad Request` dengan pesan error database.
*   **Penyebab:** Kolom `username` di database MySQL didefinisikan sebagai `NOT NULL`, namun objek entitas di Spring Boot `User.java` tidak mengirimkan nilai `username` karena tidak dipetakan.
*   **Solusi:** Pastikan file `User.java`, `RegisterRequest.java`, dan `AuthService.java` telah diperbarui untuk merekam `username` yang dikirim dari frontend, dan proyek backend telah dibangun kembali (`mvn compile`) serta direstart.

### 3. Halaman Web Error "502 Bad Gateway"
*   **Gejala:** Saat mengakses domain VNet di browser, muncul halaman error putih bertuliskan "502 Bad Gateway" dari Nginx.
*   **Penyebab:** Server Nginx aktif, namun backend Spring Boot (port 8080) dalam kondisi mati/crash.
*   **Solusi:**
    1.  Cek status service backend:
        ```bash
        sudo systemctl status vnet-backend.service
        ```
    2.  Jika statusnya `inactive` atau `failed`, baca penyebab crash di log:
        ```bash
        sudo journalctl -u vnet-backend.service -e
        ```
    3.  Perbaiki konfigurasi env variable (misalnya password database salah) lalu jalankan kembali:
        ```bash
        sudo systemctl restart vnet-backend.service
        ```
