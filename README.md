# Daily Progress Tracker

A lightweight, self-hosted daily habit tracker.
Users define their own habits and check them off each day.
Built to be simple, fast, and future-proof.

---

## What It Does

- Users register and define personal habits to track
- Each day, a simple checklist lets users tick off completed habits
- History is preserved indefinitely for review
- Admins can monitor user activity (no sensitive data exposed)
- Phase 2 adds streaks, charts, and completion summaries
- Phase 3/4 adds Discord and WhatsApp bot check-in support

---

## Tech Stack

| Layer            | Technology                |
|------------------|---------------------------|
| Backend          | Java 21 / Spring Boot 3.3 |
| Frontend         | React (Vite)              |
| Styling          | Tailwind CSS              |
| Database         | PostgreSQL 16             |
| Migrations       | Flyway                    |
| Auth             | Spring Security + JWT     |
| Containerisation | Docker Compose            |

---

## Project Structure

```
daily-progress-tracker/
│
├── backend/
│   ├── src/main/java/com/tracker/
│   │   ├── auth/               # Registration, login, JWT
│   │   ├── habit/              # Habit CRUD and status management
│   │   ├── checkin/            # Daily check-in logic
│   │   ├── admin/              # Admin-only views
│   │   └── common/
│   │       ├── config/         # Security config, CORS, beans
│   │       ├── exception/      # AppException, ErrorCode, GlobalExceptionHandler
│   │       ├── logging/        # MdcRequestFilter
│   │       └── response/       # ApiResponse envelope
│   │
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── logback-spring.xml
│   │   └── db/migration/       # Flyway SQL files (V1__, V2__, ...)
│   │
│   └── Dockerfile
│
├── frontend/
│   ├── src/
│   │   ├── pages/              # Login, Register, Dashboard, Habits, Admin
│   │   ├── components/         # Reusable UI components
│   │   ├── api/                # Axios API client — all calls centralised here
│   │   └── context/            # Auth context (JWT state)
│   └── Dockerfile
│
├── docker-compose.yml
├── .env.example                # All env vars documented here
├── .gitignore
└── README.md
```

---

## Running Locally

### Prerequisites

- Docker and Docker Compose installed
- Nothing else required on the host machine

### Steps

```bash
# 1. Clone the repository
git clone <repo-url>
cd daily-progress-tracker

# 2. Set up environment
cp .env.example .env
# Edit .env — at minimum set DB_PASSWORD and JWT_SECRET

# 3. Start everything
docker compose up --build

# 4. Access
#   Backend  → http://localhost:8080
#   Frontend → http://localhost:5173  (Step 6 onwards)
#   Health   → http://localhost:8080/actuator/health
```

---

## Docker Build Reference

```bash
# ── Full builds ─────────────────────────────────────────────────

# Build and start everything (standard)
docker compose up --build

# Build with no cache (clean build — use when dependencies change)
docker compose build --no-cache
docker compose up

# Start without rebuilding (use when only config changes)
docker compose up

# ── Selective builds ────────────────────────────────────────────

# Build and start backend only
docker compose up --build backend

# Start database only (useful during backend local dev)
docker compose up db

# Start database and backend, not frontend
docker compose up db backend

# ── Teardown ────────────────────────────────────────────────────

# Stop all containers (data preserved)
docker compose down

# Stop and remove all data volumes (full reset)
docker compose down -v

# ── Logs ────────────────────────────────────────────────────────

# Follow all logs
docker compose logs -f

# Follow backend logs only
docker compose logs -f backend

# ── Verbose logging (no rebuild required) ───────────────────────

# Edit .env:
#   LOG_LEVEL_APP=DEBUG
#   LOG_LEVEL_SQL=DEBUG
# Then:
docker compose restart backend
```

---

## Environment Variables

| Variable           | Description                            | Default / Example                   |
|--------------------|----------------------------------------|-------------------------------------|
| `DB_NAME`          | PostgreSQL database name               | `tracker_db`                        |
| `DB_USER`          | PostgreSQL username                    | `tracker_user`                      |
| `DB_PASSWORD`      | PostgreSQL password                    | *(required)*                        |
| `JWT_SECRET`       | Secret for signing JWT tokens (64+ chars) | *(required)*                     |
| `JWT_EXPIRY_HOURS` | Token validity in hours                | `24`                                |
| `ADMIN_EMAIL`      | Seed admin account email               | `admin@tracker.local`               |
| `VITE_API_BASE_URL`| API URL for the React frontend         | `http://localhost:8080`             |
| `LOG_LEVEL_APP`    | Log level for application code         | `INFO` (set `DEBUG` for verbose)    |
| `LOG_LEVEL_SQL`    | Hibernate SQL logging                  | `OFF` (set `DEBUG` to see queries)  |
| `LOG_LEVEL_SECURITY` | Spring Security filter chain         | `INFO`                              |

Full variable reference in `.env.example`.

---

## API Overview

All endpoints are prefixed `/api/v1/`.
Protected endpoints require: `Authorization: Bearer <token>`

```
AUTH
  POST   /api/v1/auth/register               Register a new user
  POST   /api/v1/auth/login                  Login, returns JWT

HABITS
  GET    /api/v1/habits                       List active habits (current user)
  POST   /api/v1/habits                       Create a habit
  PUT    /api/v1/habits/{id}                  Edit a habit
  PATCH  /api/v1/habits/{id}/archive          Archive a habit
  PATCH  /api/v1/habits/{id}/restore          Restore an archived habit
  DELETE /api/v1/habits/{id}                  Permanently delete a habit
  GET    /api/v1/habits/archived              List archived habits

CHECK-INS
  GET    /api/v1/checkins/today               Today's habits with check-in status
  POST   /api/v1/checkins/bulk                Submit today's check-ins
  GET    /api/v1/checkins/history/{habitId}   Paginated history for a habit

ADMIN  (role: ADMIN required)
  GET    /api/v1/admin/users                  List all users (no credentials/PII)
  GET    /api/v1/admin/users/{id}/activity    Habit + check-in counts for a user

HEALTH
  GET    /actuator/health                     Service health status
```

All responses use the envelope:
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-05-04T10:00:00Z"
}
```

---

## Database Schema

```
users      : id, username, email, password_hash, role, created_at
habits     : id, user_id, name, description, status (ACTIVE/ARCHIVED/DELETED), created_at, updated_at
checkins   : id, habit_id, user_id, date, is_checked, created_at
             UNIQUE (habit_id, user_id, date)
```

Schema is managed exclusively via Flyway migrations in `src/main/resources/db/migration/`.
Never modify a migration file that has already been run — always create a new one.

---

## Architecture

```
React Frontend  →  /api/v1/  →  Spring Boot 3
                                  ├── API Layer        (controllers only, no logic)
                                  ├── Service Layer    (all business logic)
                                  ├── Repository Layer (data access only)
                                  └── Domain Layer     (pure Java models)
                                         ↓
                                   PostgreSQL 16
                                  (Flyway migrations)
```

Future bots (Discord, WhatsApp) plug into the same `/api/v1/` endpoints.
New phases add packages — they do not modify existing ones.

---

## Development Phases

| Phase | Scope                                      | Status       |
|-------|--------------------------------------------|--------------|
| 1     | Auth, habits, daily check-in, admin        | 🔨 In Progress |
| 2     | Streaks, charts, summary views             | 🔲 Pending    |
| 3     | Discord bot integration                    | 🔲 Pending    |
| 4     | WhatsApp bot, reminders, export            | 🔲 Pending    |

---

## Extending the Project

To add a new feature:
1. Add a Flyway migration if schema changes are needed (`Vn__description.sql`)
2. Add a new module package under `src/main/java/com/tracker/`
3. Expose endpoints under `/api/v1/`
4. Add the corresponding React page/component under `frontend/src/`
5. Update this README and the master checklist

---

## Security Notes

- Passwords hashed with BCrypt — never stored in plain text
- JWT tokens are stateless; logout is handled client-side
- No sensitive data (passwords, tokens, PII) ever appears in log output
- Admin endpoints are role-gated at the Spring Security level
- All secrets are environment variables — never hardcoded
