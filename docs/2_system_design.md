# System Design, API Specifications & Design System - VNet

Dokumen ini mendefinisikan rancangan teknis antarmuka API (API Contract) dan standardisasi visual (Design System) yang digunakan dalam platform VNet. Dokumen ini menjadi jembatan antara tim Frontend dan Backend agar integrasi berjalan mulus.

---

## Bagian I: Spesifikasi API (API Contract)

Semua endpoint API memiliki prefix `/api` dan mengembalikan respon dalam format JSON. Setiap request yang membutuhkan autentikasi wajib menyertakan header:
`Authorization: Bearer <jwt_token>`

### 1. Autentikasi & Keamanan (`/api/auth`)

#### 1.1 Ambil Captcha
*   **Endpoint:** `GET /api/auth/captcha`
*   **Autentikasi:** Tidak
*   **Response (200 OK):**
    ```json
    {
      "captchaId": "755d1966-4e78-40be-9b4f-92f53dbc9403",
      "question": "13 - 9 = ?"
    }
    ```

#### 1.2 Registrasi Awal (Register-Init)
*   **Endpoint:** `POST /api/auth/register-init`
*   **Autentikasi:** Tidak
*   **Request Body:**
    ```json
    {
      "nama": "Muhammad Robi",
      "username": "robi12",
      "email": "robifrmsyah04@gmail.com",
      "phone": "08123456789",
      "password": "password123",
      "role": "FINANCE",
      "captchaId": "755d1966-4e78-40be-9b4f-92f53dbc9403",
      "captchaAnswer": "4"
    }
    ```
*   **Response (210 Created):**
    ```json
    {
      "message": "Kode OTP registrasi telah dikirim ke email",
      "email": "robifrmsyah04@gmail.com",
      "expiresInMinutes": 5,
      "verified": false,
      "sessionId": "robifrmsyah04@gmail.com"
    }
    ```

#### 1.3 Konfirmasi Registrasi (Verify OTP)
*   **Endpoint:** `POST /api/auth/register-confirm`
*   **Autentikasi:** Tidak
*   **Request Body:**
    ```json
    {
      "sessionId": "robifrmsyah04@gmail.com",
      "otp": "391467"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "message": "Email berhasil diverifikasi",
      "email": "robifrmsyah04@gmail.com",
      "verified": true
    }
    ```

#### 1.4 Kirim Ulang OTP
*   **Endpoint:** `POST /api/auth/resend-otp`
*   **Autentikasi:** Tidak
*   **Request Body:**
    ```json
    {
      "sessionId": "robifrmsyah04@gmail.com"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "message": "Kode OTP baru telah dikirim ke email",
      "email": "robifrmsyah04@gmail.com",
      "expiresInMinutes": 5,
      "verified": false
    }
    ```

#### 1.5 Login Masuk
*   **Endpoint:** `POST /api/auth/login-init`
*   **Autentikasi:** Tidak
*   **Request Body:**
    ```json
    {
      "username": "robi12", 
      "password": "password123",
      "captchaId": "optional-id",
      "captchaAnswer": "optional-ans"
    }
    ```
    *(Catatan: `username` bisa diisi alamat email atau username)*
*   **Response (200 OK):**
    ```json
    {
      "id": 2,
      "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIi...",
      "email": "robifrmsyah04@gmail.com",
      "name": "Muhammad Robi",
      "role": "ADMIN",
      "isVerified": true
    }
    ```

---

### 2. Modul Pelanggan (`/api/customers`)

#### 2.1 Paginasi Pelanggan (Server-Side Pagination)
*   **Endpoint:** `GET /api/customers/page?page={page_num}&size={size}&search={query}`
*   **Autentikasi:** Ya
*   **Response (200 OK):**
    ```json
    {
      "content": [
        {
          "id": 14,
          "nama": "Andi Wijaya",
          "noTelpon": "0877123456",
          "status": "ACTIVE",
          "paketName": "10 Mbps",
          "hargaPaket": 150000,
          "alamatLengkap": "Jl. Merdeka No. 4, Kel. Gambir, Kec. Gambir, Jakarta Pusat"
        }
      ],
      "totalPages": 5,
      "totalElements": 48,
      "size": 10,
      "number": 0
    }
    ```

#### 2.2 Buat Pelanggan Baru
*   **Endpoint:** `POST /api/customers`
*   **Autentikasi:** Ya
*   **Request Body:**
    ```json
    {
      "nama": "Andi Wijaya",
      "noTelpon": "0877123456",
      "status": "ACTIVE",
      "agentId": 2,
      "packageId": 1,
      "alamat": "Jl. Merdeka No. 4",
      "kelurahan": "Gambir",
      "kecamatan": "Gambir",
      "kota": "Jakarta Pusat"
    }
    ```
*   **Response (201 Created):** Objek pelanggan yang berhasil dibuat.

---

### 3. Modul Agen & Wilayah (`/api/agents` & `/api/addresses`)

#### 3.1 Performa Agen
*   **Endpoint:** `GET /api/agents/performance?period={month|year|all}`
*   **Autentikasi:** Ya
*   **Response (200 OK):**
    ```json
    [
      {
        "agentId": 2,
        "agentName": "Robi Lapangan",
        "totalAcquisitions": 18,
        "activeCommission": 900000.00,
        "status": "ACTIVE"
      }
    ]
    ```

#### 3.2 Analitik Persebaran Alamat (Address Insights)
*   **Endpoint:** `GET /api/addresses/insights`
*   **Autentikasi:** Ya
*   **Response (200 OK):**
    ```json
    [
      {
        "kelurahan": "Gambir",
        "kecamatan": "Gambir",
        "kota": "Jakarta Pusat",
        "customerCount": 24,
        "activeRate": 0.85
      }
    ]
    ```

---

## Bagian II: UI/UX Design System

VNet menganut sistem desain modern yang solid untuk memberikan tampilan premium yang bersih dan futuristik.

### 1. Palet Warna (Color System)
Sistem warna dikonfigurasi melalui *CSS Variables* di `index.css` agar konsisten di seluruh View.

```css
:root {
  /* Brand Colors */
  --color-primary: #1a2170;       /* Deep Navy - Warna utama brand */
  --color-secondary: #38bdf8;     /* Sky Blue - Warna aksen teknologi */
  --color-accent-gold: #f5a623;   /* Amber Gold - Status Isolir / Revenue */
  --color-accent-teal: #06b6d4;   /* Vivid Cyan - Visualisasi data positif */
  
  /* Semantic Status */
  --color-success: #10b981;       /* Emerald Green - Status Aktif */
  --color-danger: #ef4444;        /* Crimson Red - Status Churn / Hapus */
  --color-warning: #f5a623;
  
  /* Neutral Palette */
  --color-bg-main: #f8faff;       /* Background utama aplikasi */
  --color-bg-card: #ffffff;       /* Background box / card */
  --color-text-main: #0f172a;     /* Teks utama (slate-900) */
  --color-text-sub: #64748b;      /* Teks keterangan (slate-500) */
  --color-border: #e2e8f0;        /* Garis pemisah (slate-200) */
  
  /* Effects */
  --radius-lg: 16px;
  --radius-md: 10px;
  --radius-sm: 6px;
  --shadow-premium: 0 10px 25px -5px rgba(26, 33, 112, 0.05), 0 8px 10px -6px rgba(26, 33, 112, 0.05);
}
```

### 2. Tipografi (Typography)
*   **Primary Font:** `Inter`, sans-serif (diambil dari Google Fonts).
*   **Heading:** `Outfit` atau `Inter` dengan weight semi-bold (600) atau bold (700).
*   **Body Copy:** `Inter` dengan weight regular (400) atau medium (500) untuk tingkat keterbacaan yang tinggi.

### 3. Transisi & Animasi Mikro (Micro-interactions)
Untuk meningkatkan *User Experience* (UX), sistem memiliki animasi transisi internal yang didefinisikan secara seragam:
1.  **Hover Scale:** Tombol utama dan KPI Card membesar perlahan saat diarahkan kursor (`transform: translateY(-2px)`, transition `cubic-bezier(0.4, 0, 0.2, 1)`).
2.  **Shake Error:** Form yang mengalami error validasi akan bergetar singkat (`shake` keyframe animation) untuk menarik perhatian pengguna.
3.  **Fade Slide View:** Perpindahan halaman Single Page menggunakan transisi *fade-slide* secara halus untuk meminimalkan kesan lag visual.
