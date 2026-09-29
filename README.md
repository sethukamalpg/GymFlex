# 🏋️ GymFlex — Gym Membership & Attendance Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Render-Deploy%20Ready-46E3B7?style=for-the-badge&logo=render&logoColor=black)](https://render.com/)
[![Database](https://img.shields.io/badge/Database-H2%20%7C%20PostgreSQL%20%7C%20MySQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](#database-configuration)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/sethukamalpg/GymFlex)

**GymFlex** is a full-stack, enterprise-grade Gym Membership and Attendance Management application built with **Spring Boot** and **Thymeleaf**. Designed with a modern, dark-neon glassmorphic UI, GymFlex provides gym owners, administrators, and fitness centers with an intuitive, unified platform to track member subscriptions, monitor real-time daily check-ins, automate renewal workflows, and preemptively flag expiring memberships.

---

## 🌟 Key Features

### 👤 Member Management
- **Instant Registration**: Register new members with validated contact information (name, email, phone) and immediately enroll them in a membership plan.
- **Detailed Member Profiles**: Comprehensive member view with active plan details, validity timelines, lifetime check-in logs, and current month attendance totals.
- **Duplicate Prevention**: Automated validation prevents duplicate email registrations across the database.

### 💳 Plans & Membership Lifecycles
- **Preconfigured Tiers**: Out-of-the-box support for Monthly, Quarterly, and Yearly plans (auto-seeded on startup).
- **Automated Expiry Calculation**: Expiration dates are calculated automatically from the registration date based on plan duration.
- **One-Click Renewal**: Seamless membership renewals that append duration to the current active end-date or reset validity from today if expired.
- **Dynamic Plan Creation**: REST APIs and extensible schemas to configure custom membership tiers and pricing.

### ⏱️ Attendance & Check-In Tracking
- **Smart Digital Check-In**: Record member check-ins with one click from the member profile or central attendance desk.
- **Active Membership Enforcement**: Intelligent validation blocks check-in attempts if a member's subscription is expired.
- **Real-Time Logs & Relative Timestamps**: Live attendance feed showing check-in history, total daily check-ins, and monthly stats.

### 🔔 Proactive Expiry Monitoring
- **7-Day Expiry Watchlist**: Dedicated notification center flagging all memberships set to expire within the next 7 days.
- **Dashboard Alerts**: Immediate visual badges and action shortcuts allowing front-desk staff to renew memberships before they lapse.

### 📊 Real-Time Analytics Dashboard
- **Key Performance Metrics**: Live counters for Total Members, Active Memberships, Expiring Soon, and Today's Check-Ins.
- **Recent Activity Feed**: Instant view of the latest member entries.
- **Quick Actions**: Rapid shortcuts to add new members, log attendance, or inspect plans.

### 🔒 Security & Route Protection
- **Session-Based Authentication**: Secure login gateway with `AuthInterceptor` route guard protecting all management endpoints.
- **Configurable Credentials**: Easy environment-variable-driven admin access (`GYMFLEX_ADMIN_USERNAME` and `GYMFLEX_ADMIN_PASSWORD`).

### 🚀 Production & Cloud Ready
- **Multi-Database Agility**: Runs with zero setup on embedded in-memory **H2**, and switches seamlessly to **PostgreSQL** or **MySQL** via environment variables.
- **Optimized Multi-Stage Dockerfile**: Layer-cached Maven build resulting in a slim, secure, unprivileged runtime container (`eclipse-temurin:17-jre-jammy`).
- **Render 1-Click Blueprint**: Built-in `render.yaml` specification for continuous deployment on Render.

---

## 🛠️ Tech Stack & Architecture

| Layer | Technology | Details |
|---|---|---|
| **Backend Framework** | Spring Boot 4.1.1 | WebMVC, Data JPA, Validation, DevTools |
| **Language** | Java 17 | Modern LTS features, Records, Pattern Matching |
| **Frontend / Templates** | Thymeleaf 3.x | Server-side templating with modular layout fragments |
| **Styling** | Custom Vanilla CSS | Glassmorphism, CSS Grid & Flexbox, Inter typography, Responsive |
| **Database (Dev)** | H2 Database | Embedded in-memory database with web console enabled |
| **Database (Prod)** | PostgreSQL / MySQL | Production drivers included (`postgresql`, `mysql-connector-j`) |
| **ORM / Persistence** | Hibernate 6.x / Spring Data JPA | Auto schema migration (`update`), custom JPQL queries |
| **Containerization** | Docker | Multi-stage build with non-root security execution |
| **Cloud Hosting** | Render | Automated Docker blueprint with dynamic port binding |

---

## 📁 Project Structure

```text
GymFlex/
├── Dockerfile                     # Multi-stage Docker packaging configuration
├── render.yaml                    # Render Cloud Blueprint specification
├── pom.xml                        # Maven dependencies & build plugins
├── mvnw / mvnw.cmd                # Maven Wrapper scripts (Linux/macOS/Windows)
└── src/
    └── main/
        ├── java/com/example/GymFlex/
        │   ├── GymFlexApplication.java     # Main Spring Boot entry point
        │   ├── config/                     # Configuration beans & interceptors
        │   │   ├── AuthInterceptor.java    # Route protection interceptor
        │   │   ├── DataInitializer.java    # Default plan database seeder
        │   │   └── WebMvcConfig.java       # Interceptor registration & static paths
        │   ├── controller/                 # HTTP Request Controllers
        │   │   ├── MemberController.java   # REST API for members & attendance
        │   │   ├── PageController.java     # Thymeleaf UI page routes & auth flow
        │   │   └── PlanController.java     # REST API for membership plans
        │   ├── dto/                        # Data Transfer Objects
        │   │   ├── AttendanceResponse.java
        │   │   ├── MemberListItemDto.java
        │   │   └── RegisterMemberRequest.java
        │   ├── exception/                  # Custom exceptions & global handler
        │   │   ├── DuplicateResourceException.java
        │   │   ├── GlobalExceptionHandler.java
        │   │   ├── MembershipExpiredException.java
        │   │   └── ResourceNotFoundException.java
        │   ├── models/                     # JPA Database Entities
        │   │   ├── CheckIn.java            # Attendance check-in entity
        │   │   ├── Member.java             # Gym member personal details
        │   │   ├── Membership.java         # Membership validity & status
        │   │   └── Plan.java               # Subscription plans entity
        │   ├── repository/                 # Spring Data JPA Repositories
        │   │   ├── CheckInRepository.java
        │   │   ├── MemberRepository.java
        │   │   ├── MembershipRepository.java
        │   │   └── PlanRepository.java
        │   └── services/                   # Business logic layer
        │       ├── MemberService.java
        │       └── PlanService.java
        └── resources/
            ├── application.properties      # Spring datasource & app settings
            ├── static/                     # Static web assets
            │   ├── css/style.css           # Glassmorphism dark UI styling
            │   ├── images/                 # Graphics & icons
            │   └── js/                     # Client-side scripts
            └── templates/                  # Thymeleaf HTML Templates
                ├── login.html              # Authentication gateway
                ├── dashboard.html          # Main overview dashboard
                ├── members.html            # Member directory & search
                ├── add-member.html         # Member onboarding form
                ├── member-details.html     # Profile, attendance & renewal
                ├── plans.html              # Membership tiers & pricing
                ├── attendance.html         # Check-in counter & attendance logs
                ├── expiring-members.html   # 7-day expiration watchlist
                ├── error.html              # Custom error page
                └── fragments/              # Reusable UI partials (header, nav)
```

---

## ⚡ Quick Start (Run Locally)

### Prerequisites
- **Java Development Kit (JDK) 17** or higher installed
- **Git** installed
- *(Optional)* **Docker** if running as a container

### 1. Clone the Repository
```bash
git clone https://github.com/sethukamalpg/GymFlex.git
cd GymFlex
```

### 2. Run the Application
The project includes the Maven Wrapper. By default, it runs on an embedded **in-memory H2 database** without requiring any external database installation.

**On Windows (PowerShell / Command Prompt):**
```powershell
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
./mvnw spring-boot:run
```

### 3. Access GymFlex in Your Browser
- **Web Application URL**: [http://localhost:8080](http://localhost:8080)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:gymflex`
  - Username: `sa`
  - Password: *(leave blank)*

### 4. Default Login Credentials
| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | System Manager / Gym Admin |

> Credentials can be overridden using the environment variables `GYMFLEX_ADMIN_USERNAME` and `GYMFLEX_ADMIN_PASSWORD`.

---

## 🗄️ Database Configuration

GymFlex is designed with flexible database connectivity. You can easily switch between databases using standard Spring environment variables:

### 1. In-Memory H2 (Default — Zero Setup)
```properties
# No additional setup required! Automatically configured in application.properties:
spring.datasource.url=jdbc:h2:mem:gymflex;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
```

### 2. PostgreSQL (Render, Neon, Supabase, AWS RDS, Local)
Set the following environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gymflex
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password
```

### 3. MySQL (Aiven, TiDB, Railway, Local)
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/gymflex?useSSL=false&serverTimezone=UTC
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=your_password
```

---

## 🐳 Docker Setup

### Run with Docker Locally
GymFlex provides a production-grade multi-stage Docker build:

```bash
# 1. Build the Docker image
docker build -t gymflex .

# 2. Run the container
docker run -p 8080:8080 -e PORT=8080 gymflex
```

Open [http://localhost:8080](http://localhost:8080) in your browser.

---

## ☁️ Deployment on Render

GymFlex includes a native `render.yaml` Blueprint for 1-click cloud deployment.

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/sethukamalpg/GymFlex)

### Option A: 1-Click Instant Blueprint Deploy
1. Click the **Deploy to Render** button above or open:
   👉 **[https://render.com/deploy?repo=https://github.com/sethukamalpg/GymFlex](https://render.com/deploy?repo=https://github.com/sethukamalpg/GymFlex)**
2. Sign in to your [Render Dashboard](https://dashboard.render.com/).
3. Render reads `render.yaml` automatically and configures:
   - **Service Name**: `gymflex`
   - **Runtime**: Docker (using multi-stage [`Dockerfile`](./Dockerfile))
   - **Port**: Dynamically bound via `$PORT`
   - **Admin Credentials**: `admin` / `admin123` (or customize them in the Environment Variables table)
   - **Auto-Deploy**: Enabled on branch `main`
4. Click **Apply** to launch your live instance.

### Option B: Automatic Continuous Deployment (If already deployed)
- Whenever new code is pushed to `origin/main`, Render detects the commit automatically and triggers a fresh build and zero-downtime deployment.
- You can monitor the deployment progress live under **Events** or **Logs** in your Render Dashboard.

---

## 🔌 REST API Reference

GymFlex provides a comprehensive RESTful API for integrations with mobile applications, kiosk terminals, or biometric scanners.

### Member Endpoints (`/api/members`)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/members` | Register a new gym member with a plan |
| `GET` | `/api/members` | Retrieve all members |
| `GET` | `/api/members/{memberId}` | Get details of a specific member |
| `PUT` | `/api/members/{memberId}` | Update member personal details and membership plan |
| `DELETE` | `/api/members/{memberId}` | Safely delete a member, check-ins, and membership |
| `PUT` | `/api/members/{memberId}/renew` | Renew membership for an existing member |
| `POST` | `/api/members/{memberId}/checkin` | Record member check-in |
| `GET` | `/api/members/expiring` | Get all memberships expiring within 7 days |
| `GET` | `/api/members/{memberId}/attendance` | Get current month's attendance count |

#### Example: Register Member (`POST /api/members`)
```json
{
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "phone": "+1-555-0199",
  "planId": 1
}
```

#### Example: Check-In Response (`POST /api/members/1/checkin`)
```json
{
  "id": 1,
  "checkInTime": "2026-09-29T08:30:00",
  "member": {
    "id": 1,
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "phone": "+1-555-0199"
  }
}
```

---

### Plan Endpoints (`/api/plans`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/plans` | Fetch all available membership plans |
| `GET` | `/api/plans/{id}` | Get plan details by ID |
| `POST` | `/api/plans` | Create a new membership plan tier |

#### Example: Create Plan (`POST /api/plans`)
```json
{
  "name": "Semi-Annual VIP",
  "durationMonths": 6,
  "price": 5000.0
}
```

---

## 🖥️ Web UI Navigation

| Route | Page | Purpose |
|---|---|---|
| `/` | **Login Gateway** | Administrative credentials check and session setup |
| `/dashboard` | **Analytics Dashboard** | Overview of member counts, active subscriptions, recent check-ins |
| `/members` | **Members Directory** | Comprehensive list of all registered members with quick actions |
| `/members/add` | **New Member Onboarding** | Form to register a member and assign an initial plan |
| `/members/{id}/edit` | **Edit Member Profile** | Modify member personal details, phone, email, and plan tier |
| `/members/{id}` | **Member Details** | Member status, membership duration, one-click renew, and check-in history |
| `/attendance` | **Attendance Center** | Daily check-in desk, monthly attendance counters, and activity feed |
| `/members/expiring`| **Expiring Watchlist** | Proactive warning list of memberships expiring in the next 7 days |
| `/plans` | **Plans & Pricing** | Overview of active membership tiers, durations, and pricing |
| `/logout` | **Logout** | Invalidate active session and return to login screen |

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to check the [issues page](https://github.com/sethukamalpg/GymFlex/issues) if you want to contribute.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

<p align="center">
  Crafted with ❤️ for modern gym operations and fitness centers.
</p>
