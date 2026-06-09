# Deployment & DevOps Guide - VNet

Dokumen ini menjelaskan prosedur pemindahan aplikasi VNet dari komputer lokal (*development*) ke server produksi (*production*) menggunakan server Linux (Ubuntu/Debian) dengan Nginx sebagai reverse proxy.

---

## Bagian I: Kompilasi & Build Produksi

### 1. Build Frontend (Vue 3)
1.  Masuk ke folder frontend dan buat file `.env.production` jika belum ada:
    ```
    VITE_API_BASE_URL=/api
    ```
2.  Jalankan perintah build:
    ```bash
    npm run build
    ```
3.  Perintah ini akan menghasilkan direktori `dist/` yang berisi file HTML, JS, dan CSS statis yang sangat teroptimasi.

### 2. Build Backend (Spring Boot 3)
1.  Masuk ke folder backend:
    ```bash
    mvn clean package -DskipTests
    ```
2.  Perintah ini menghasilkan file executable JAR di dalam direktori `target/`, misalnya `vnet_backend-0.0.1-SNAPSHOT.jar`.

---

## Bagian II: Konfigurasi Server Produksi

### 1. Setup Backend sebagai Systemd Service
Untuk memastikan backend Spring Boot berjalan secara otomatis di latar belakang dan melakukan restart otomatis jika server mati, buat file service di `/etc/systemd/system/vnet-backend.service`:

```ini
[Unit]
Description=VNet ISP Backend Application
After=syslog.target network.target mysql.service

[Service]
User=vnetuser
ExecStart=/usr/bin/java -jar /var/www/vnet/backend/vnet_backend-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

# Environment variables produksi
Environment=DB_USERNAME=vnet_db_user
Environment=DB_PASSWORD=SecureDbPassword123!
Environment=SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/vnet_db?useSSL=false
Environment=JWT_SECRET=SangatRahasiaDanPanjangBanget32KarakterAtauLebih!
Environment=RESEND_API_KEY=re_YourResendApiKey

[Install]
WantedBy=multi-user.target
```

Jalankan perintah berikut untuk mengaktifkan service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable vnet-backend.service
sudo systemctl start vnet-backend.service
```

---

### 2. Konfigurasi Nginx (Reverse Proxy & Static Files)
Nginx bertugas menyajikan file frontend statis secara instan dan meneruskan request API ke backend Spring Boot (Port 8080).

Buat file konfigurasi baru di `/etc/nginx/sites-available/vnet.conf`:

```nginx
server {
    listen 80;
    server_name vnet.victorynetwork.id;

    # Redirect HTTP ke HTTPS (setelah SSL terpasang)
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name vnet.victorynetwork.id;

    # Sertifikat SSL Let's Encrypt
    ssl_certificate /etc/letsencrypt/live/vnet.victorynetwork.id/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/vnet.victorynetwork.id/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # Folder static frontend Vue 3
    root /var/www/vnet/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API ke Spring Boot
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # Timeout adjustment
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

Aktifkan konfigurasi dan restart Nginx:
```bash
sudo ln -s /etc/nginx/sites-available/vnet.conf /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

---

## Bagian III: Strategi Backup Database

Untuk mengamankan data pelanggan dari insiden hardware crash, setup cron job harian untuk mencadangkan database MySQL.

1.  Buat script backup di `/var/www/vnet/backup_db.sh`:
    ```bash
    #!/bin/bash
    BACKUP_DIR="/var/www/vnet/backups"
    DB_NAME="vnet_db"
    DATE=$(date +%Y-%m-%d_%H%M%S)
    
    # Buat backup sql terkompresi
    mysqldump -u root -p"SecureDbPassword123!" $DB_NAME | gzip > "$BACKUP_DIR/vnet_backup_$DATE.sql.gz"
    
    # Hapus backup yang lebih tua dari 30 hari
    find $BACKUP_DIR -type f -name "*.sql.gz" -mtime +30 -delete
    ```
2.  Beri izin eksekusi script:
    ```bash
    chmod +x /var/www/vnet/backup_db.sh
    ```
3.  Daftarkan di crontab untuk dieksekusi setiap tengah malam (jam 00:00):
    ```bash
    0 0 * * * /var/www/vnet/backup_db.sh
    ```
