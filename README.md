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
cd frontend
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

Portfolio ready (priority order):
- [ ] Add a short demo video or GIF and screenshots to this README.
- [x] Fix local dev instructions (`cd client` should be `cd frontend`).
- [x] Remove debug `console.log` left in production game code.
- [ ] Add unit tests for core game logic (win detection, move validation).
- [ ] Add an architecture diagram (e.g. Mermaid) to this README.
- [ ] Document the WebSocket message protocol (message types and payloads).

Production and real userbase readiness (priority order):
- [ ] TLS + domain (CSRF is currently disabled — only safe over HTTPS).
- [ ] Re-enable CSRF protection or enforce `SameSite=Strict` session cookies.
- [ ] Rate limiting on `/login`, `/register`, and game action endpoints.
- [ ] Pin Docker image versions (`postgres`, `redis`) to avoid unexpected breakage.
- [ ] WebSocket reconnect logic so players can recover from dropped connections.
- [ ] Observability: structured logging, metrics, and alerting.

## License

MIT. See `LICENSE`.
