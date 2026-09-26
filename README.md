# 📦 GMS Backend Service

Main business-logic microservice for the **Global Medical System**: patient/doctor/admin flows,
appointments, consent management, and the server-rendered web portal.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-green)
![Vue](https://img.shields.io/badge/Vue-3-brightgreen)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow)

## 🚀 Quick Start

```bash
mvn spring-boot:run
# Server starts on http://localhost:8080
```

Open http://localhost:8080 in a browser to access the portal.

## 🔧 Tech Stack
- **Java 21**, **Spring Boot 3.3.0**
- **Thymeleaf** server-rendered pages
- **Vue 3** components embedded in selected views
- **Spring Security** + JWT
- **Spring Data JPA** + **MySQL 8**
- **Maven**

## 🖥️ Web Views
- `/login`, `/register` — authentication
- `/patient/dashboard` — patient self-service portal
- `/doctor/dashboard` — doctor workspace (consent-gated)
- `/admin/dashboard` — admin controls
- Vue components are embedded in `templates/` and `static/`

## 🔐 Roles & Access Matrix

| Role | Can view patients | Can edit records | Can manage users | Can give consent |
|------|:---:|:---:|:---:|:---:|
| **ADMIN**  | ✅ all | ✅ | ✅ | ❌ |
| **DOCTOR** | ✅ only consented patients | ❌ | ❌ | ❌ |
| **PATIENT**| ✅ self only | ✅ self only | ❌ | ✅ |

> 🔒 A patient must explicitly grant consent before a doctor can view their records.
> Consent management lives in this service.

## 📂 REST Endpoints (selected)

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST   | /api/auth/login        | public | Issue JWT |
| POST   | /api/auth/register     | public | Register new patient |
| GET    | /api/patients/me       | JWT    | Patient self-view |
| GET    | /api/patients/{id}     | JWT+consent | View one patient |
| POST   | /api/consent           | JWT    | Grant/revoke consent |
| GET    | /api/appointments      | JWT    | List appointments |

## 🐳 Docker

```bash
docker build -t gms-backend-service .
docker run -p 8080:8080 gms-backend-service
```

## ⚙️ Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/gms_db` | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `gms_user` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `gms_pass` | DB password |
| `JWT_SECRET` | — | HMAC secret for JWT signing |
| `FILE_SERVICE_URL` | `http://localhost:8081` | Where file-system-service runs |

## 🧪 Build & Test

```bash
mvn clean package
mvn test
mvn spring-boot:run
```

---

Part of the GMS ecosystem. See the main project: [golamrabbanyshikder/global-medical-system](https://github.com/golamrabbanyshikder/global-medical-system)
