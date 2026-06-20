# ====================================================
# PANDUAN DEPLOY VNET KE VPS - STEP BY STEP
# ====================================================

## 📋 FILE YANG PERLU DI-UPLOAD KE VPS

```
Yang perlu di-upload:
├── backend/target/vnet-backend-0.0.1-SNAPSHOT.jar  → ke /var/www/vnet/backend/
├── frontend/dist/ (semua isinya)                   → ke /var/www/vnet/frontend/
├── deploy/nginx.conf                               → ke /etc/nginx/sites-available/vnet
└── deploy/vnet-backend.service                     → ke /etc/systemd/system/
```

---

## 🚀 LANGKAH-LANGKAH DEPLOY

### STEP 1 — Login SSH ke VPS
```bash
ssh root@IP_VPS_LO
```

---

### STEP 2 — Jalankan Deploy Script
Upload dulu deploy.sh ke VPS, lalu jalankan:
```bash
chmod +x deploy.sh
./deploy.sh
```
Script ini otomatis install: Java 17, MySQL, Nginx

---

### STEP 3 — Upload File dari Laptop ke VPS
Buka PowerShell di laptop, jalankan:

```powershell
# Upload backend .jar
scp "c:\vnet-projek\backend\target\vnet-backend-0.0.1-SNAPSHOT.jar" root@IP_VPS:/var/www/vnet/backend/

# Upload frontend (semua isi dist/)
scp -r "c:\vnet-projek\frontend\dist\*" root@IP_VPS:/var/www/vnet/frontend/

# Upload Nginx config
scp "c:\vnet-projek\deploy\nginx.conf" root@IP_VPS:/etc/nginx/sites-available/vnet

# Upload service file
scp "c:\vnet-projek\deploy\vnet-backend.service" root@IP_VPS:/etc/systemd/system/

# Upload .env.example sebagai template .env di VPS
scp "c:\vnet-projek\deploy\.env.example" root@IP_VPS:/var/www/vnet/backend/.env
```

### STEP 3.5 — Konfigurasi .env di VPS
Setelah file terupload, buka SSH VPS dan jalankan perintah berikut untuk mengedit konfigurasi database & JWT rahasia milik VPS:
```bash
# Buka file .env di VPS
nano /var/www/vnet/backend/.env

# Ganti password database dan JWT secret key sesuai kebutuhan, lalu simpan (Ctrl+O, Enter, Ctrl+X)
```

---

### STEP 4 — Import Database (di VPS)
```bash
# Import SQL database
mysql -u vnetuser -p vnet_db < /var/www/vnet/backend/database.sql
```

---

### STEP 5 — Setup Nginx (di VPS)
```bash
# Aktifkan config vnet
ln -s /etc/nginx/sites-available/vnet /etc/nginx/sites-enabled/vnet

# Hapus config default
rm /etc/nginx/sites-enabled/default

# Test config Nginx
nginx -t

# Restart Nginx
systemctl restart nginx
```

---

### STEP 6 — Jalankan Backend (di VPS)
```bash
# Daftarkan service
systemctl daemon-reload

# Enable service (auto start saat boot)
systemctl enable vnet-backend

# Jalankan service
systemctl start vnet-backend

# Cek status (pastikan running)
systemctl status vnet-backend
```

---

### STEP 7 — Cek Semuanya Berjalan
```bash
# Cek backend jalan di port 8080
curl http://localhost:8080/api/health

# Cek Nginx
systemctl status nginx

# Lihat log backend kalau ada error
tail -f /var/www/vnet/logs/backend.log
```

---

## ✅ DONE!
Buka browser → ketik IP VPS lo → website harus muncul! 🎉

---

## 🔧 TROUBLESHOOTING

### Backend tidak mau jalan?
```bash
# Lihat log error
tail -100 /var/www/vnet/logs/backend-error.log

# Cek Java terinstall
java -version

# Cek MySQL jalan
systemctl status mysql
```

### Website tidak muncul di browser?
```bash
# Cek Nginx config
nginx -t

# Lihat log Nginx
tail -f /var/log/nginx/vnet_error.log
```

### Database error?
```bash
# Masuk MySQL
mysql -u root

# Cek database ada
SHOW DATABASES;

# Cek user ada
SELECT user FROM mysql.user;
```
