# Developer Setup, Code Conventions & Configuration Guide - VNet

Dokumen ini ditulis khusus untuk *Software Engineer* baru yang bergabung dalam pengembangan proyek VNet. Panduan ini menjelaskan instalasi lokal, arsitektur folder, standar penulisan kode, dan konfigurasi lingkungan (*environment variables*).

---

## Bagian I: Local Development Setup

### 1. Prasyarat Sistem (Prerequisites)
Pastikan perangkat lokal Anda telah terinstal software berikut:
*   **Java Development Kit (JDK):** Versi 17 (Rekomendasi: Eclipse Temurin atau Azul Zulu).
*   **Node.js:** Versi 18 LTS atau lebih baru.
*   **Build Tool:** Apache Maven 3.8+.
*   **DBMS:** MySQL Community Server 8.0+.
*   **IDE:** IntelliJ IDEA (Rekomendasi untuk Spring Boot) dan VS Code (Rekomendasi untuk Vue 3).

### 2. Langkah Instalasi & Menjalankan Aplikasi

#### 2.1 Clone & Setup Database
1.  Buat database kosong bernama `vnet_db` di MySQL Anda:
    ```sql
    CREATE DATABASE vnet_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```

#### 2.2 Menjalankan Backend (Spring Boot 3)
1.  Masuk ke direktori backend:
    ```bash
    cd backend
    ```
2.  Lakukan instalasi dependensi dan kompilasi:
    ```bash
    mvn clean compile
    ```
3.  Set password database Anda sebagai env variable, kemudian jalankan:
    *   **Windows (PowerShell):**
        ```powershell
        $env:SPRING_DATASOURCE_PASSWORD="password_mysql_kamu"; mvn spring-boot:run
        ```
    *   **Linux / macOS:**
        ```bash
        SPRING_DATASOURCE_PASSWORD="password_mysql_kamu" mvn spring-boot:run
        ```
4.  Backend akan berjalan pada port **8080** (`http://localhost:8080`).

#### 2.3 Menjalankan Frontend (Vue 3 + Vite)
1.  Masuk ke direktori frontend:
    ```bash
    cd ../frontend
    ```
2.  Install dependensi npm:
    ```bash
    npm install
    ```
3.  Jalankan server development:
    ```bash
    npm run dev
    ```
4.  Aplikasi web dapat diakses di browser pada URL yang tertera di terminal (biasanya `http://localhost:5173`).

---

## Bagian II: Konfigurasi Lingkungan (Environment Variables)

### 1. Backend (`backend/src/main/resources/application.properties`)
Daftar variabel yang dapat dikonfigurasi melalui sistem env variable:

| Variabel Env | Deskripsi | Default Value |
| :--- | :--- | :--- |
| `DB_USERNAME` | Username MySQL | `root` |
| `DB_PASSWORD` | Password MySQL | *(Kosong)* |
| `RESEND_API_KEY` | API Key dari Resend untuk kirim email | *(Kosong - fallback ke log)* |
| `JWT_SECRET` | Kunci enkripsi token JWT | `vnet_dev_only_secret_key...` |
| `SEED_ADMIN_ENABLED` | Aktifkan pembuatan admin otomatis saat start | `false` |

### 2. Frontend (`frontend/.env.development` & `.env.production`)
*   `VITE_API_BASE_URL`: Menentukan URL base dari API server Spring Boot.
    *   Development: `http://localhost:8080/api`
    *   Production: `/api` (jika berada di balik reverse proxy yang sama).

---

## Bagian III: Aturan Penulisan Kode (Code Conventions)

### 1. Struktur Folder Proyek

#### 1.1 Backend (Java Spring Boot)
Struktur package yang bersih mengikuti pola arsitektur layer:
*   `config/`: Menyimpan file konfigurasi (Security, JWT, CORS, Bean).
*   `controller/`: Menyimpan REST Controllers (endpoint pemetaan HTTP).
*   `service/`: Tempat menulis seluruh *business logic* (tidak boleh ditulis di controller/repository).
*   `repository/`: Interface Spring Data JPA untuk interaksi database.
*   `entity/`: Representasi tabel database (menggunakan anotasi `@Entity` Jakarta Persistence).
*   `dto/`: *Data Transfer Object* untuk memetakan request body dan custom response.
*   `enums/`: Kumpulan data konstan bertipe ENUM (misal: `Role`, `Status`).

#### 1.2 Frontend (Vue 3 Single Page Application)
Struktur di dalam `frontend/src`:
*   `assets/`: File static seperti gambar, logo, dan global CSS.
*   `components/`: UI komponen reusable yang independen (misal: layout topbar, detail panel).
*   `composables/`: Fungsi pembantu yang reusable (Vue composables).
*   `router/`: Konfigurasi Vue Router untuk navigasi halaman.
*   `services/`: Berisi kode integrasi API (file `api.js` menggunakan Fetch API).
*   `stores/`: Menyimpan state global aplikasi (opsional Pinia).
*   `views/`: Halaman utama aplikasi yang di-mount oleh router (Signup, Login, Dashboard, dll).

### 2. Standar Penamaan (Naming Conventions)
*   **Java (Backend):**
    *   Nama Class: `PascalCase` (Contoh: `AuthService`, `RegisterRequest`).
    *   Variabel & Method: `camelCase` (Contoh: `verifyOtp`, `rawOtp`).
    *   Konstanta: `UPPER_SNAKE_CASE` (Contoh: `DEFAULT_EXPIRATION_MINUTES`).
*   **JavaScript & Vue (Frontend):**
    *   Nama File Komponen: `PascalCase` (Contoh: `SignupView.vue`, `KpiCard.vue`).
    *   Fungsi & Variabel: `camelCase`.
*   **CSS:**
    *   Nama Class: Metodologi BEM dengan pemisah dash (Contoh: `.field-wrap`, `.field-wrap--focus`).
*   **Database (MySQL):**
    *   Nama Tabel: `snake_case` jamak (Contoh: `users`, `internet_packages`).
    *   Nama Kolom: `snake_case` (Contoh: `created_at`, `otp_expired_at`).
