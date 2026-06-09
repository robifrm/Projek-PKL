# Business Requirements Document (BRD) & Software Requirements Specification (SRS) - VNet

Dokumen ini menjelaskan analisis kebutuhan bisnis dan spesifikasi kebutuhan perangkat lunak untuk sistem **VNet ISP Management Platform**. Dokumen ini dirancang sebagai panduan tunggal (*single source of truth*) yang mendefinisikan mengapa platform ini dibangun (BRD) dan bagaimana perilakunya secara mendetail (SRS).

---

## Bagian I: Business Requirements Document (BRD)

### 1. Latar Belakang & Visi Produk
Dalam industri penyedia jasa internet (*Internet Service Provider* / ISP), efisiensi operasional sangat menentukan keberlangsungan bisnis. Banyak penyedia ISP lokal menghadapi masalah operasional klasik:
*   **Kehilangan Pelanggan (*Churn*):** Sulit mendeteksi tanda-tanda pelanggan yang akan berhenti berlangganan sebelum terlambat.
*   **Kurangnya Visibilitas Kinerja Agen:** Tidak adanya alat terpusat untuk memantau performa mitra agen di lapangan dan kontribusi akuisisi pelanggan mereka.
*   **Alamat Pemasangan yang Tidak Terstandardisasi:** Kesulitan menganalisis wilayah potensial karena data alamat yang acak-acakan.
*   **Masalah Keamanan Autentikasi:** Rentannya portal administrasi dari serangan bot dan registrasi tidak sah.

**Visi VNet:** Menjadi platform manajemen operasional terintegrasi kelas enterprise untuk ISP, menyediakan kontrol terpusat atas data pelanggan, performa penjualan agen, analisis persebaran geografi, dengan visualisasi dashboard yang memukau dan aman.

### 2. Stakeholders & Target Pengguna
Sistem ini melayani beberapa aktor utama:
1.  **Administrator / Pemilik ISP (Super Admin):** Memiliki akses penuh ke semua metrik keuangan, manajemen paket, penghapusan data, dan hak akses penuh.
2.  **Network Operations Center (NOC) / Admin Operasional:** Mengelola operasional harian pelanggan, memantau status aktif/isolir, dan melihat tren gangguan/wilayah.
3.  **Mitra / Agen Lapangan:** Tenaga penjualan di lapangan yang didaftarkan untuk memperluas jangkauan instalasi pelanggan.

### 3. Metrik Kesuksesan (Success Metrics)
*   **User Retention & Churn Reduction:** Membantu menurunkan tingkat isolir/churn sebesar 15% melalui identifikasi awal tren nonaktif.
*   **Security Strength:** Menekan registrasi bot palsu hingga 100% menggunakan tantangan keamanan Captcha dinamis dan OTP email.
*   **Data Integrity:** Standardisasi 100% alamat pemasangan menggunakan modul input terstruktur (Alamat, Kelurahan, Kecamatan, Kota).

---

## Bagian II: Software Requirements Specification (SRS)

### 1. Kebutuhan Fungsional (Functional Requirements)

Sistem wajib menyediakan fitur-fitur berikut yang dikelompokkan ke dalam beberapa modul utama:

#### 1.1 Modul Keamanan & Autentikasi (Auth)
*   **Tantangan Captcha Dinamis:** Sistem harus menyajikan soal matematika sederhana acak (misal: `13 - 9 = ?`) setiap kali registrasi atau login untuk mencegah serangan otomatisasi bot.
*   **Dua Tahap Registrasi (Register-Init & Confirm):**
    *   Pengguna mengisi data nama, username, email, telepon, password, serta captcha.
    *   Sistem memvalidasi data dan mengirimkan kode OTP 6-digit ke email pendaftar.
    *   Sistem menyimpan data dalam status *unverified* agar proses pendaftaran tidak hilang jika pengiriman email gagal.
*   **Verifikasi OTP Email:** Pengguna memasukkan 6-digit kode OTP untuk mengaktifkan akun. Kode OTP berlaku selama 5 menit.
*   **Kirim Ulang OTP (Resend OTP):** Menyediakan mekanisme kirim ulang kode OTP jika pengguna tidak menerima email pertama, menggunakan email/username sebagai pengenal.
*   **Dual-Credential Login:** Mendukung masuk ke sistem menggunakan alamat email **atau** username yang terdaftar.

#### 1.2 Modul Dashboard & Analytics
*   **KPI Cards Utama:** Menampilkan ringkasan metrik real-time:
    *   Total Pelanggan Aktif
    *   Total Agen Terdaftar
    *   Rasio Churn / Isolir (Persentase pelanggan isolir dibanding total pelanggan)
    *   Estimasi Pendapatan Bulanan (Total harga paket pelanggan aktif)
*   **Live Charts Visualisasi:**
    *   **Tren Pertumbuhan Pelanggan:** Grafik garis bulanan yang memetakan jumlah pelanggan aktif vs isolir.
    *   **Distribusi Paket Layanan:** Grafik lingkaran/donat yang menunjukkan persentase paket internet terpopuler.
*   **Filter Analitik:** Pengguna dapat memfilter visualisasi dashboard berdasarkan bulan/tahun tertentu.

#### 1.3 Modul Manajemen Pelanggan (Customer CRUD)
*   **Input Pelanggan Baru:** Menyimpan data nama pelanggan, nomor telepon, alamat pemasangan (relasi alamat), paket internet yang dipilih, dan agen penanggung jawab.
*   **Ubah Status Berlangganan:** Mendukung perubahan status pelanggan secara instan:
    *   `ACTIVE`: Pelanggan aktif menikmati internet.
    *   `ISOLIR`: Internet dinonaktifkan sementara karena telat membayar.
    *   `INACTIVE`: Langganan dihentikan sepenuhnya.
*   **Pencarian & Pagination Server-side:** Mendukung pencarian nama pelanggan dan wilayah pemasangan secara real-time dengan pemuatan data bertahap (*pagination*) agar browser tidak hang.

#### 1.4 Modul Manajemen Agen & Komisi
*   **Perekaman Data Agen:** CRUD data nama agen dan status operasional (`ACTIVE` / `INACTIVE`).
*   **Performa Akuisisi:** Merekam berapa banyak pelanggan yang didaftarkan oleh masing-masing agen.
*   **Penghitungan Komisi:** Menghitung akumulasi komisi agen berdasarkan formula bisnis yang ditentukan.

#### 1.5 Modul Manajemen Wilayah & Paket (Metadata)
*   **Address Insights:** Menganalisis konsentrasi pelanggan per kelurahan/kecamatan untuk membantu ekspansi jaringan infrastruktur (kabel/tiang FO).
*   **CRUD Paket Internet:** Pengelolaan katalog paket internet (Nama Paket, Kecepatan dalam Mbps, Harga Bulanan).

---

### 2. Kebutuhan Non-Fungsional (Non-Functional Requirements)

#### 2.1 Keamanan (Security)
*   **Enkripsi Data Sensitif:** Semua password pengguna wajib di-hash menggunakan algoritma **BCrypt** sebelum disimpan di database.
*   **Token-Based Session:** Autentikasi API menggunakan token **JWT (JSON Web Token)** yang kedaluwarsa dalam 24 jam.
*   **CORS Policy:** Membatasi akses API server hanya dari domain frontend yang dipercaya (dalam mode produksi).

#### 2.2 Performa & Skalabilitas (Performance & Scalability)
*   **Response Time:** Waktu respon API server untuk transaksi dasar (non-analitik) harus di bawah **200ms**.
*   **JPA Query Optimization:** Operasi pengambilan data analitik besar wajib menghindari masalah query N+1 menggunakan `JOIN FETCH` atau proyeksi native query MySQL.
*   **Server-Side Aggregation:** Perhitungan analitik pendapatan dan statistik geografi harus dihitung langsung di sisi database (MySQL `SUM`, `COUNT`, `GROUP BY`) bukan memuat ratusan ribu baris data ke memory Java.

#### 2.3 Usabilitas & Tampilan (Usability & Aesthetics)
*   **Responsive Layout:** UI harus tetap proporsional dan berfungsi baik di resolusi Desktop (>= 1280px), Tablet (768px - 1024px), hingga Mobile (< 768px).
*   **Aesthetics Premium:** Menggunakan visual modern (Navy, Sky Blue, dan Gold accents), tipografi Google Fonts (Inter), visual feedback berupa skeleton loaders, micro-animations pada hover, dan Dark Mode native.
