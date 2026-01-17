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

- Frontend: React + TypeScript + Vite + Tailwind CSS
- Backend: Spring Boot 3, WebSocket, JPA, Spring Security
- Data: PostgreSQL, Redis, Flyway
- Infra: Docker, Nginx reverse proxy

## Architecture

High level request flow:

- Browser -> Nginx
- Nginx serves the static frontend and proxies `/api/*` (including `/api/ws`) to the backend.
- Backend handles REST auth/lobby endpoints and WebSocket game events.
- PostgreSQL stores users and game results.
- Redis stores sessions and ephemeral state.

## Quickstart (Docker)

Prereqs: Docker and Docker Compose.

```bash
export FINAR_POSTGRES_PASSWORD=your_password

docker compose up --build
```

Then open `http://localhost:8889`.

## Local Development

### 1) Start dependencies

```bash
export FINAR_POSTGRES_PASSWORD=your_password

docker compose up postgres redis
```

### 2) Run the backend

```bash
cd backend

export POSTGRES_HOST=localhost
export POSTGRES_USERNAME=app
export POSTGRES_PASSWORD=$FINAR_POSTGRES_PASSWORD
export REDIS_HOST=localhost

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend runs on `http://localhost:8000` in dev.

### 3) Run the frontend

```bash
cd client
npm install
npm run dev
```

Vite serves the app on `http://localhost:5173` and proxies `/api` to the backend.

## Environment Variables

Required for local or Docker:

- `FINAR_POSTGRES_PASSWORD`: Password for the Postgres `app` user.
- `POSTGRES_HOST`: Hostname for Postgres (defaults to container name in Docker).
- `POSTGRES_USERNAME`: Postgres username (default `app`).
- `POSTGRES_PASSWORD`: Postgres password.
- `REDIS_HOST`: Redis hostname (defaults to container name in Docker).

## Game Rules

- Players alternate turns placing stones on the board.
- First player to connect five stones in a row (horizontal, vertical, or diagonal) wins.

## Roadmap

Portfolio ready:
- [ ] Add a short demo video or GIF and screenshots to this README.
- [ ] Write an architecture overview with a diagram and key design decisions.
- [ ] Add a clear API/WebSocket message reference.
- [ ] Add automated tests for game logic and matchmaking flows.
- [ ] Polish UI/UX, onboarding, and accessibility (keyboard, ARIA, contrast).
- [ ] Document deployment steps and local dev troubleshooting.

Production and real userbase readiness:
- [ ] Harden matchmaking, reconnect logic, and spectator mode.
- [ ] Implement anti-cheat and server-authoritative game validation.
- [ ] Add rate limiting, abuse prevention, and moderation tooling.
- [ ] Add account security (email verification, password reset, optional OAuth).
- [ ] Production hosting with TLS, domain, and CDN for static assets.
- [ ] Observability: metrics, logs, tracing, and alerting.
- [ ] Backups, migrations, and data retention policy.
- [ ] Scale-out readiness: load balancing, sticky sessions for WebSockets, Redis clustering.
- [ ] Growth features: leaderboards, profiles, matchmaking rating, and analytics.

## License

MIT. See `LICENSE`.
