#!/bin/bash

# ============================================
# VNET PROJECT - VPS DEPLOY SCRIPT
# Jalankan script ini di VPS setelah login SSH
# ============================================

echo "======================================"
echo "  VNET - Auto Deploy Script"
echo "======================================"

# ---- KONFIGURASI (GANTI SESUAI KEBUTUHAN) ----
DB_NAME="vnet_db"
DB_USER="vnetuser"
DB_PASS="VNet@DB2026!"
APP_DIR="/var/www/vnet"
JAR_NAME="vnet-backend-0.0.1-SNAPSHOT.jar"
# -----------------------------------------------

echo ""
echo "[1/7] Update sistem..."
apt update -y && apt upgrade -y

echo ""
echo "[2/7] Install Java 17..."
apt install -y openjdk-17-jdk
java -version

echo ""
echo "[3/7] Install MySQL..."
apt install -y mysql-server
systemctl start mysql
systemctl enable mysql

echo ""
echo "[4/7] Setup Database MySQL..."
mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
EOF
echo "Database '$DB_NAME' berhasil dibuat!"

echo ""
echo "[5/7] Install Nginx..."
apt install -y nginx
systemctl start nginx
systemctl enable nginx

echo ""
echo "[6/7] Buat folder aplikasi..."
mkdir -p $APP_DIR/frontend
mkdir -p $APP_DIR/backend
mkdir -p $APP_DIR/logs

echo ""
echo "[7/7] Setup selesai!"
echo ""
echo "======================================"
echo "  LANGKAH SELANJUTNYA:"
echo "======================================"
echo "1. Upload file ke VPS (lihat panduan)"
echo "2. Import database SQL"
echo "3. Jalankan: sudo systemctl start vnet-backend"
echo "4. Akses website di browser"
echo "======================================"
