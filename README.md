# FINAR

FINAR (Five In A Row) is an online multiplayer game inspired by Gomoku. Players take turns placing stones and try to be the first to connect five in a row.

## Demo

- TODO: Record a short gameplay video or GIF and add screenshots.

## Features

- Real-time multiplayer gameplay over WebSockets.
- Lobbies and matchmaking flow for creating or joining games.
- In-game chat and game event notifications.
- Session-based auth (register/login) with Redis-backed sessions.
- Game results persisted in PostgreSQL with Flyway migrations.

## Tech Stack

- Frontend: React, TypeScript, Vite, TailwindCSS
- Backend: Spring Boot 3, WebSockets, Hibernate, Spring Security
- Data: PostgreSQL, Redis, Flyway
- Infra: Docker

## Quickstart (Docker)

Prereqs: Docker and Docker Compose.

```bash
export FINAR_POSTGRES_PASSWORD=your_password

docker compose up --build
```

Then open `http://localhost:8889`.

## Local Development

### 1) Start dependencies

If you have Docker installed:

```bash
export FINAR_POSTGRES_PASSWORD=your_password

docker compose up postgres redis
```

If not, you will have to manually setup:
1. PostgreSQL - user needs flyway privileges
2. Redis

### 2) Run the backend

```bash
cd backend

export POSTGRES_HOST=localhost
export POSTGRES_USERNAME=app
export POSTGRES_PASSWORD='your psql user pw'
export REDIS_HOST=localhost

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend runs on `http://localhost:8000` in dev.

### 3) Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Vite serves the app on `http://localhost:5173` and proxies `/api` to the backend.

## Environment Variables

- `FINAR_POSTGRES_PASSWORD`: Password for the Postgres `app` user.
- `POSTGRES_HOST`: Hostname for Postgres (defaults to container name in Docker).
- `POSTGRES_USERNAME`: Postgres username (default `app`).
- `POSTGRES_PASSWORD`: Postgres password.
- `REDIS_HOST`: Redis hostname (defaults to container name in Docker).

## Game Rules

- Players alternate turns placing stones on the board.
- First player to connect five stones in a row (horizontal, vertical, or diagonal) wins.

## Roadmap

TBD - reviving the project

## License

MIT. See `LICENSE`.
