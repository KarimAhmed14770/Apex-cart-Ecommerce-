# Kee V2C Platform

A full-stack multi-vendor e-commerce REST API built with Spring Boot, featuring JWT authentication, role-based access control, real-time SSE vendor notifications, and a vanilla JS frontend.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Data JPA |
| Database | MySQL + Flyway migrations |
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
- Vendor oversight

**Architecture highlights**
- SOLID principles applied across service layer (SRP, OCP, DIP)
- `@TransactionalEventListener(AFTER_COMMIT)` — notifications only fire after the transaction commits, preventing ghost notifications on rollback
- One-time stream ticket (30 s Caffeine TTL) for SSE authentication without exposing JWT in query params
- JPA `Specification` API for dynamic multi-field product filtering
- Flyway for reproducible schema versioning

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

### 2. Create the database

```sql
CREATE DATABASE kee_v2c_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Flyway will create all tables automatically on first startup.

### 3. Configure application properties

```bash
cp application.properties.example src/main/resources/application.properties
```

Then open `src/main/resources/application.properties` and fill in:

| Property | Description |
|---|---|
| `spring.datasource.username` | Your MySQL username |
| `spring.datasource.password` | Your MySQL password |
| `application.security.jwt.secret-key` | Base64 secret, min 256 bits (`openssl rand -base64 32`) |
| `uploads_directory` | Absolute path to an existing writable directory for images |
| `app.cors.allowed-origins` | Frontend origin, e.g. `http://localhost:8080` |

### 4. Run the application

```bash
./mvnw spring-boot:run
```

Or on Windows:

```bash
mvnw.cmd spring-boot:run
```

The server starts on **http://localhost:8080**.

### 5. Explore the API

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

The frontend is served at:

```
http://localhost:8080/index.html        # Customer storefront
http://localhost:8080/vendor.html       # Vendor dashboard
http://localhost:8080/admin.html        # Admin panel
```

## Running Tests

Tests use an H2 in-memory database and do not require MySQL to be running.

```bash
./mvnw test
```

The test suite covers:

- **`CartStockValidationTest`** — unit tests for stock decrement logic (Mockito)
- **`GlobalExceptionHandlerTest`** — unit tests for 409 idempotency and 400 stock error responses
- **`JwtSecurityTest`** — integration tests verifying that protected endpoints reject unauthenticated requests and public endpoints remain accessible

## Project Structure

```
src/
├── main/
│   ├── java/com/Kee/V2C/
│   │   ├── config/          # CORS, web config
│   │   ├── entity/          # JPA entities
│   │   ├── dto/             # Request/response DTOs
│   │   ├── Repository/      # Spring Data JPA repositories
│   │   ├── rest/            # REST controllers
│   │   ├── security/        # JWT filter, security config, UserDetails
│   │   ├── service/         # Business logic (one interface per domain)
│   │   ├── events/          # Spring application events (OrderPlacedEvent)
│   │   └── exception/       # Global exception handler
│   └── resources/
│       ├── db/migration/    # Flyway SQL migrations
│       └── static/          # Frontend (HTML, CSS, JS)
└── test/
    └── java/com/Kee/V2C/
        ├── security/        # JwtSecurityTest
        ├── service/cart/    # CartStockValidationTest
        └── exception/       # GlobalExceptionHandlerTest
```

## API Overview

| Domain | Base Path | Role |
|---|---|---|
| Authentication | `/api/auth/**` | Public |
| Categories & Brands | `/api/categories/**`, `/api/brands/**` | Public |
| Products | `/api/products/**` | Customer / Vendor |
| Cart | `/api/carts/**` | Customer |
| Checkout | `/api/checkouts` | Customer |
| Orders | `/api/orders/**` | Customer |
| Invoices | `/api/invoices/**` | Customer |
| Vendor profile | `/api/vendors/**` | Vendor |
| Shop management | `/api/shops/**` | Vendor |
| Stock management | `/api/stocks/**` | Vendor |
| Sub-orders | `/api/sub-orders/**` | Vendor |
| Notifications (SSE) | `/api/notifications/**` | Vendor |
| Product models | `/api/product-models/**` | Vendor / Admin |
| Admin | `/api/admin/**` | Admin |
