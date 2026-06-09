# 🚀 Panduan Setup Proyek VNet — Untuk Kolaborator

---

## 📋 Prasyarat Wajib

Pastikan software berikut sudah terinstall di komputer:

| Software          | Versi Minimal | Link Download |
|-------------------|--------------|---------------|
| Java JDK          | 17+           | https://adoptium.net |
| Node.js           | 18+           | https://nodejs.org |
| MySQL Server      | 8.0+          | https://dev.mysql.com/downloads |
| Git               | Terbaru       | https://git-scm.com |

---

## ⬇️ 1. Clone Repositori

```bash
git clone https://github.com/robifrm/project-InternshipVNET.git
cd project-InternshipVNET
```

---

## 🗄️ 2. Setup Database MySQL

### A. Buat database kosong
Login ke MySQL lalu jalankan:
```sql
CREATE DATABASE vnet_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### B. (Opsional) Import data awal
Jika ada file `.sql` dump dari tim, import dengan:
```bash
mysql -u root -p vnet_db < data_dump.sql
```

---

## ⚙️ 3. Konfigurasi Backend (Spring Boot)

### A. Sesuaikan file `application.properties`
Buka file: `backend/src/main/resources/application.properties`

Ubah baris berikut sesuai dengan konfigurasi MySQL lokal Anda:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vnet_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta
spring.datasource.username=root          # ← Ganti dengan username MySQL kamu
spring.datasource.password=password123   # ← Ganti dengan password MySQL kamu
```

### B. (Opsional) Konfigurasi Email OTP via Resend
Jika ingin fungsi registrasi & OTP via email aktif, daftar di https://resend.com dan isi:
```properties
resend.api.key=re_YOUR_API_KEY_HERE
resend.from-email=VNet <noreply@yourdomain.com>
```
> ⚠️ **Catatan:** Tanpa API key Resend, gunakan OTP bypass developer: `123456` atau `000000`

---

## 🖥️ 4. Jalankan Backend

```bash
cd backend
./mvnw spring-boot:run       # Linux/Mac
.\mvnw.cmd spring-boot:run   # Windows (PowerShell)
```

Backend berjalan di: **http://localhost:8080**

---

## 🎨 5. Setup Frontend (Vue 3 + Vite)

### A. Install dependency Node.js
```bash
cd frontend
npm install
```
> Ini akan mengunduh ~250MB folder `node_modules/` (tidak ada di GitHub karena di `.gitignore`)

### B. Jalankan development server
```bash
npm run dev
```

Frontend berjalan di: **http://localhost:5174**

---

## 🔑 6. Login Pertama Kali

Setelah backend berjalan, buat akun baru melalui halaman `/signup`.

**Kode OTP Developer (tidak perlu email asli):**
```
123456   atau   000000
```

---

## 📁 File yang TIDAK Ada di GitHub (Harus Dibuat Sendiri)

| File/Folder | Alasan | Cara Membuat |
|---|---|---|
| `frontend/node_modules/` | Terlalu besar | `npm install` di folder frontend |
| `frontend/dist/` | Build artifact | `npm run build` (opsional, hanya untuk produksi) |
| `mysql-data/` | Data lokal database | Otomatis dibuat oleh MySQL saat `spring-boot:run` pertama |
| `backend/target/` | Hasil kompilasi Maven | Otomatis dibuat saat menjalankan backend |

---

## ✅ Verifikasi Berhasil

Jika semua langkah di atas berhasil, Anda seharusnya bisa:
- [ ] Membuka http://localhost:5174 dan melihat halaman Login VNet
- [ ] Mendaftar akun baru dengan OTP `123456`
- [ ] Masuk ke Dashboard dan melihat data analytics

---

## 🆘 Troubleshooting Umum

| Error | Solusi |
|---|---|
| `Public Key Retrieval is not allowed` | Pastikan `allowPublicKeyRetrieval=true` ada di JDBC URL |
| `Port 8080 already in use` | Matikan proses lain di port 8080, atau ubah `server.port` di `application.properties` |
| `CORS error` di browser | Pastikan backend sudah jalan di `localhost:8080` sebelum membuka frontend |
| OTP tidak terkirim ke email | Gunakan kode bypass `123456` untuk pengujian lokal |
