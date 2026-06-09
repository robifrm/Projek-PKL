# VNet Dashboard + Backend

Struktur terbaik untuk development tetap dua aplikasi:

- `C:\vnet-dashboard` untuk Vue/Vite UI.
- `C:\vnet-backend` untuk Spring Boot API.

Untuk production, sistemnya dibuat satu pintu: Spring Boot serve API dan hasil build Vue dari folder `src/main/resources/static`.

## Development

Jalankan backend:

```powershell
cd C:\vnet-backend
.\mvnw.cmd spring-boot:run
```

Jalankan dashboard:

```powershell
cd C:\vnet-dashboard
npm run dev
```

Dashboard development tetap buka dari `http://127.0.0.1:5173`, dan request `/api` diproxy ke backend `8080`.

## Production Satu Sistem

Build dashboard, copy ke backend, lalu build Spring Boot:

```powershell
cd C:\vnet-dashboard
.\scripts\build-one-system.ps1
```

Setelah itu jalankan backend:

```powershell
cd C:\vnet-backend
.\mvnw.cmd spring-boot:run
```

Buka sistem dari:

```text
http://127.0.0.1:8080
```
