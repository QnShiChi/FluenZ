## Context

The FluenZ backend has Spring Boot + JPA + PostgreSQL configured with a User entity that already contains `email`, `username`, `passwordHash`, and `currentLevel` fields. We need to add JWT-based authentication on top of this existing structure. The frontend has Zustand for state management and Axios for API calls.

## Goals / Non-Goals

**Goals:**
- Stateless JWT authentication with access + refresh tokens
- BCrypt password hashing on registration
- Spring Security filter chain protecting all endpoints except auth and health
- Frontend login/register pages with Shadcn UI components
- Automatic token injection via Axios interceptor
- Token persistence in localStorage

**Non-Goals:**
- OAuth2 / social login (future change)
- Email verification (future change)
- Role-based authorization / admin roles (future change)
- Password reset flow (future change)

## Decisions

### 1. JWT Library: JJWT (io.jsonwebtoken)
**Decision:** Use `jjwt` 0.12.x for JWT operations.
**Rationale:** Most popular Java JWT library, well-maintained, supports HMAC-SHA256 signing. Spring Security doesn't include JWT support out of the box.

### 2. Token Strategy: Access + Refresh
**Decision:** Issue a short-lived access token (24h) and a longer-lived refresh token (7 days).
**Rationale:** Access tokens expire quickly to limit exposure. Refresh tokens allow seamless re-authentication without re-entering credentials. For initial development, 24h access token is generous enough to avoid friction.

### 3. Token Storage: localStorage
**Decision:** Store JWT tokens in `localStorage` on the frontend.
**Rationale:** Simple to implement. HttpOnly cookies would require additional backend CSRF config. For this MVP, localStorage is sufficient. Can migrate to cookies later for enhanced security.

### 4. Password Hashing: BCrypt
**Decision:** Use Spring Security's `BCryptPasswordEncoder`.
**Rationale:** Industry standard, built into Spring Security, configurable work factor.

### 5. Security Filter Chain
**Decision:** Use `OncePerRequestFilter` subclass (`JwtAuthenticationFilter`) that:
1. Extracts JWT from `Authorization: Bearer <token>` header
2. Validates the token
3. Sets `SecurityContext` authentication
**Rationale:** Standard Spring Security pattern. The filter runs before every request, making auth transparent to controllers.

### 6. Public Endpoints
**Decision:** The following endpoints are public (no auth required):
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/health`
**Rationale:** Auth endpoints must be accessible without a token. Health check is used by Docker.

## Risks / Trade-offs

- **localStorage XSS risk** → Mitigation: acceptable for MVP, will migrate to HttpOnly cookies before production
- **No token revocation** → Mitigation: short-lived tokens + refresh flow. Can add blacklist later
- **Single JWT secret** → Mitigation: stored in env variable, rotatable via config change
