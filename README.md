# ifs25005-p5-pbo2026-springboot-cashflow

Aplikasi Cashflow (catatan keuangan pribadi): Spring Boot 4.1.2, Vaadin 25.3.4, JDBC PostgreSQL, Clean Architecture.

## Menjalankan
1. Buat database: `CREATE DATABASE pbo2026_cashflow;`
2. Salin `.env.example` menjadi `.env` lalu sesuaikan `DB_*`, `ROOT_*`, `SESSION_SECRET`.
3. Development: `.\mvnw.cmd spring-boot:run` (buka http://localhost:8080, login admin dari `ROOT_*`).
4. Produksi: `.\mvnw.cmd clean package -Pproduction` lalu `java -jar target\ifs25005-p5-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar`.
5. Tes + coverage: `.\mvnw.cmd clean verify` (laporan: `target/site/jacoco/index.html`).

> `mvnw` / `.mvn/` tidak ikut di sini: generate lewat Spring Initializr (IntelliJ New Project) atau `mvn wrapper:wrapper`.

## Rute
| Path | View | Akses |
|---|---|---|
| `/login`, `/register` | LoginView, RegisterView | Publik |
| `/` | FinanceView | USER & ADMIN |
| `/profile` | ProfileView | USER & ADMIN |
| `/users` | UsersView | ADMIN |
| `/user-transactions?userId=` | UserTransactionsView (read-only) | ADMIN |
| `/uploads/**`, `/icons/**`, `styles.css`, `robots.txt` | statis | Publik |
