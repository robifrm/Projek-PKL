# QA Testing Strategy & Scenarios - VNet

Dokumen ini mendefinisikan strategi jaminan kualitas (QA) dan skenario pengujian komprehensif untuk memastikan seluruh fitur platform VNet berjalan dengan benar dan aman sebelum dirilis ke lingkungan produksi.

---

## Bagian I: Skenario Pengujian (Test Scenarios)

### 1. Modul Registrasi & OTP
Skenario pengujian untuk memastikan pendaftaran akun aman dari bot dan berfungsi secara logis.

| ID Tes | Deskripsi Skenario | Langkah Pengujian | Hasil yang Diharapkan |
| :--- | :--- | :--- | :--- |
| **REG-01** | Registrasi dengan field tidak lengkap | Kirim data registrasi tanpa menyertakan password atau email. | Server merespon status `400 Bad Request` dengan pesan error validasi yang sesuai. |
| **REG-02** | Registrasi dengan jawaban Captcha salah | Isi formulir registrasi lengkap tapi isi jawaban Captcha asal-asalan. | Server menolak registrasi dengan status `400 Bad Request` dan pesan "Kode Captcha salah...". |
| **REG-03** | Registrasi dengan email yang sudah diverifikasi | Lakukan registrasi menggunakan email yang sudah aktif terdaftar di sistem. | Server mengembalikan status `409 Conflict` dengan pesan "Email sudah terdaftar". |
| **REG-04** | Registrasi dengan username yang sudah digunakan | Lakukan registrasi menggunakan username yang sudah aktif terdaftar di sistem. | Server mengembalikan status `409 Conflict` dengan pesan "Username sudah digunakan". |
| **REG-05** | Konfirmasi verifikasi OTP sukses | Masukkan kode OTP 6-digit yang benar yang didapat dari logs/email. | Akun aktif terverifikasi (`isVerified = true`), server mengembalikan status `200 OK`, dan user diarahkan ke login. |
| **REG-06** | Konfirmasi verifikasi OTP salah/kedaluwarsa | Masukkan kode OTP asal atau kode OTP yang sudah lewat 5 menit. | Server merespon status `400 Bad Request` dengan pesan "OTP tidak valid atau sudah kedaluwarsa". |
| **REG-07** | Kirim ulang OTP (Resend) | Klik tombol "Kirim Ulang Kode" setelah hitung mundur selesai. | OTP baru digenerate, transaksi database sukses (tidak rollback), kode OTP baru tercetak di log backend, status respon `200 OK`. |

### 2. Modul Login & Sesi
Skenario pengujian untuk menguji pintu masuk sistem.

| ID Tes | Deskripsi Skenario | Langkah Pengujian | Hasil yang Diharapkan |
| :--- | :--- | :--- | :--- |
| **LOG-01** | Login dengan Email + Password benar | Masukkan email terverifikasi dan password yang cocok. | Respon `200 OK` berisi token JWT dan info profil user. |
| **LOG-02** | Login dengan Username + Password benar | Masukkan username terverifikasi dan password yang cocok. | Respon `200 OK` berisi token JWT dan info profil user (dual-credential sukses). |
| **LOG-03** | Login dengan akun belum diverifikasi | Masukkan kredensial akun yang belum melakukan verifikasi OTP. | Server mengembalikan status `403 Forbidden` dengan pesan "Email belum diverifikasi". |
| **LOG-04** | Login dengan kredensial salah | Masukkan email/username atau password yang salah. | Server mengembalikan status `401 Unauthorized` dengan pesan "Email atau password salah". |

### 3. Modul Manajemen Pelanggan (Customer CRUD)
Skenario pengujian untuk menguji manajemen data utama.

| ID Tes | Deskripsi Skenario | Langkah Pengujian | Hasil yang Diharapkan |
| :--- | :--- | :--- | :--- |
| **CUS-01** | Tambah pelanggan baru sukses | Masukkan nama, telepon, paket internet, agen, dan alamat lengkap. | Data tersimpan di database, dan muncul di list pelanggan secara real-time. |
| **CUS-02** | Ubah status pelanggan menjadi Isolir | Ubah status pelanggan aktif menjadi `ISOLIR` di tabel pelanggan. | Status berubah di database, pendapatan bulanan di dashboard berkurang secara otomatis. |
| **CUS-03** | Hapus pelanggan | Hapus satu baris pelanggan dari sistem. | Pelanggan terhapus tanpa merusak integritas relasi tabel lainnya. |

---

## Bagian II: User Acceptance Testing (UAT) Checklist

Lembar persetujuan akhir sebelum rilis:

- [ ] **UAT-01: Keamanan & Bot Prevention**
  - [ ] Captcha memuat soal matematika baru setiap kali di-refresh.
  - [ ] Captcha wajib diisi dan divalidasi dengan benar oleh server backend.
  - [ ] Password yang disimpan di database `users` terenkripsi dengan format `$2a$...` (BCrypt).
- [ ] **UAT-02: Flow Registrasi & OTP**
  - [ ] Registrasi sukses mengirimkan OTP (muncul di logs terminal backend jika dev).
  - [ ] Tombol Kirim Ulang OTP nonaktif selama hitung mundur 60 detik berjalan.
  - [ ] User tidak bisa login sebelum status verifikasi OTP diselesaikan.
- [ ] **UAT-03: Login & Hak Akses (Auth)**
  - [ ] Login mendukung input email maupun username secara bergantian.
  - [ ] Token JWT disimpan dengan aman di `localStorage` setelah login berhasil.
  - [ ] Halaman dashboard terproteksi dari akses langsung tanpa login (Router Guard Vue berfungsi).
- [ ] **UAT-04: Tampilan Visual & Dashboard**
  - [ ] Grafik donat pembagian paket internet ter-render sempurna sesuai data real-time.
  - [ ] Angka pendapatan bulanan dan rasio churn dihitung dengan akurat oleh query server database.
  - [ ] Peralihan ke Dark Mode mengubah seluruh latar belakang dan elemen visual tanpa ada teks yang tersembunyi/tidak kontras.
