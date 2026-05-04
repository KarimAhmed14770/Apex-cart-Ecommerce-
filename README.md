# Kee V2C Platform

A full-stack multi-vendor e-commerce REST API built with Spring Boot, featuring JWT authentication, role-based access control, real-time SSE vendor notifications, and a vanilla JS frontend.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Data JPA |
| Database | MySQL 8 |
| Auth | JWT (jjwt 0.12) + role-based access control |
| Mapping | MapStruct |
| Caching | Caffeine |
| Notifications | Server-Sent Events (SSE) |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Mockito, Spring Security Test, H2 |
| Frontend | Vanilla JS, HTML, CSS |

## Features

**Customers**
- Browse products by category, brand, and filters
- Shopping cart with real-time stock validation
- Checkout with credit card or cash-on-delivery
- Order history and invoice download
- Idempotency protection on duplicate order submissions

**Vendors**
- Shop and product management (CRUD + image uploads)
- Stock management per product
- Sub-order tracking (each order splits by vendor)
- Real-time new-order notifications via SSE (no polling)

**Admin**
- Category, subcategory, and brand management
- Product model (spec template) management
- Vendor and customer oversight

**Architecture highlights**
- SOLID principles applied across the service layer (SRP, OCP, DIP)
- `@TransactionalEventListener(AFTER_COMMIT)` — notifications only fire after the transaction commits, preventing ghost notifications on rollback
- One-time stream ticket (30 s Caffeine TTL) for SSE authentication without exposing the JWT in query params
- JPA `Specification` API for dynamic multi-field product filtering

## Prerequisites

- Java 21+
- Maven 3.9+ (or use the included `mvnw` wrapper)
- MySQL 8+

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/KarimAhmed14770/Kee-V2C-Platform.git
cd Kee-V2C-Platform
```

### 2. Create the database and run the schema scripts

```sql
CREATE DATABASE `Kee_V2C_Platform` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> **Note:** The database name is case-sensitive on Linux. Use `Kee_V2C_Platform` exactly as shown — the migration scripts reference this name directly.
> If you prefer a different database name, do a find-and-replace of `Kee_V2C_Platform` across all 6 files in `src/main/resources/db/migration/` and update `spring.datasource.url` in `application.properties` to match.

Then run the SQL files in `src/main/resources/db/migration/` in the order below (order matters — foreign key dependencies). You can use MySQL Workbench, DBeaver, or the MySQL CLI:

```bash
# 1. Core users: credentials, customers, vendors, roles (no dependencies)
mysql -u YOUR_USERNAME -p Kee_V2C_Platform < src/main/resources/db/migration/V2_Create_customer_vendor_credentials_tables.sql

# 2. Catalogue: categories, sub-categories, brands, product models, products, shops, stock (needs vendors)
mysql -u YOUR_USERNAME -p Kee_V2C_Platform < src/main/resources/db/migration/V2_Create_categories_products_shops_stock.sql

# 3. Cart (needs customers + products)
mysql -u YOUR_USERNAME -p Kee_V2C_Platform < src/main/resources/db/migration/V2_Create_cart_item_table.sql

# 4. Product model requests (needs vendors)
mysql -u YOUR_USERNAME -p Kee_V2C_Platform < src/main/resources/db/migration/V2_Create_Product_requests_table.sql

# 5. Orders, sub-orders, order items (needs customers + vendors + products)
mysql -u YOUR_USERNAME -p Kee_V2C_Platform < src/main/resources/db/migration/V2_Create_customer_order_orderitems_tables.sql

# 6. Payment records (needs orders — must be last)
mysql -u YOUR_USERNAME -p Kee_V2C_Platform < src/main/resources/db/migration/V2_Create_payment_records.sql
```

### 3. Configure application properties

**Linux / macOS:**
```bash
cp application.properties.example src/main/resources/application.properties
```

**Windows:**
```cmd
copy application.properties.example src\main\resources\application.properties
```

Then open `src/main/resources/application.properties` and fill in:

| Property | Description |
|---|---|
| `spring.datasource.url` | Already set to `Kee_V2C_Platform` — change only if you named your database differently |
| `spring.datasource.username` | Your MySQL username |
| `spring.datasource.password` | Your MySQL password |
| `application.security.jwt.secret-key` | Base64 secret, min 256 bits — generate with `openssl rand -base64 32` (Linux/macOS/Git Bash) or `[Convert]::ToBase64String((1..32 \| % { [byte](Get-Random -Max 256) }))` (PowerShell) |
| `uploads_directory` | Absolute path to a **writable directory** where images will be stored (see step 4) |
| `app.cors.allowed-origins` | Frontend origin, e.g. `http://localhost:8080` |

### 4. Create the uploads directory

The application stores product and profile images on disk. Create the directory you specified in `uploads_directory` before starting the app:

```bash
# Linux / macOS — example
mkdir -p /var/uploads/v2c

# Windows — example
mkdir C:\uploads\v2c
```

### 5. Run the application

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The server starts on **http://localhost:8080**.

### 6. Create an admin account

Admin users are not created through the registration API. After the app starts, register a regular account via the frontend or API, then promote it to admin directly in MySQL:

```sql
-- Replace 'your_username' with the username you registered
UPDATE users_roles
SET role = 'ROLE_ADMIN'
WHERE user_id = (SELECT id FROM users_credentials WHERE user_name = 'your_username');

UPDATE users_credentials
SET status = 'ACTIVE'
WHERE user_name = 'your_username';
```

Then log in at `http://localhost:8080/index.html` — you will be redirected to the admin panel.

### 7. Explore the API

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

The frontend is served at:

```
http://localhost:8080/index.html     # Customer storefront
http://localhost:8080/vendor.html    # Vendor dashboard
http://localhost:8080/admin.html     # Admin panel
```

## Running Tests

Tests use an H2 in-memory database and do not require MySQL.

```bash
./mvnw test
```

| Test class | Type | What it covers |
|---|---|---|
| `CartStockValidationTest` | Unit (Mockito) | Stock decrement — insufficient stock throws, sufficient stock passes |
| `GlobalExceptionHandlerTest` | Unit | 409 on duplicate order, 400 on insufficient stock |
| `JwtSecurityTest` | Integration (MockMvc) | Protected endpoints return 403 without token or with corrupt token; public endpoints return 200 |

## Project Structure

```
src/
├── main/
│   ├── java/com/Kee/V2C/
│   │   ├── config/          # CORS and web configuration
│   │   ├── entity/          # JPA entities
│   │   ├── dto/             # Request / response DTOs
│   │   ├── Repository/      # Spring Data JPA repositories
│   │   ├── rest/            # REST controllers
│   │   ├── security/        # JWT filter, SecurityConfig, UserDetails
│   │   ├── service/         # Business logic (one interface per domain)
│   │   ├── events/          # Application events (OrderPlacedEvent)
│   │   └── exception/       # Global exception handler
│   └── resources/
│       ├── db/migration/    # SQL schema scripts (run in order — see Setup)
│       └── static/          # Frontend (HTML, CSS, JS)
└── test/
    └── java/com/Kee/V2C/
        ├── security/        # JwtSecurityTest
        ├── service/cart/    # CartStockValidationTest
        └── exception/       # GlobalExceptionHandlerTest
```

## API Overview

| Domain | Base Path | Access |
|---|---|---|
| Authentication | `/api/auth/**` | Public |
| Categories & Brands | `/api/categories/**`, `/api/brands/**` | Public |
| Products | `/api/products/**` | Customer / Vendor / Admin |
| Cart | `/api/carts/**` | Customer |
| Checkout | `/api/checkouts` | Customer |
| Orders | `/api/orders/**` | Customer |
| Invoices | `/api/invoices/**` | Customer |
| Vendor profile | `/api/vendors/**` | Vendor / Admin |
| Shop management | `/api/shops/**` | Vendor |
| Stock management | `/api/stocks/**` | Vendor |
| Sub-orders | `/api/sub-orders/**` | Vendor |
| Notifications (SSE) | `/api/notifications/**` | Vendor |
| Product models | `/api/product-models/**` | Vendor / Admin |
| Customers | `/api/customers/**` | Customer / Admin |
