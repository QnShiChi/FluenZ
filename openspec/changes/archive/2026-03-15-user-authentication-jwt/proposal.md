## Why

The database schema and entities are in place, but there's no way to authenticate users. Every feature module (Onboarding, Chunking Practice, AI Roleplay) requires knowing who the user is. Without authentication, we cannot create user-specific learning paths or track practice progress. JWT-based auth provides stateless, scalable authentication suitable for a REST API.

## What Changes

- Add Spring Security + JWT dependencies to the backend
- Create `SecurityConfig` to configure HTTP security with JWT filter
- Create `JwtService` for token generation, validation, and parsing
- Create `AuthController` with register/login/refresh/me endpoints
- Create `AuthService` for registration and login business logic with BCrypt password hashing
- Create `CustomUserDetailsService` to load users for Spring Security
- Create auth-related DTOs (LoginRequest, RegisterRequest, AuthResponse)
- Update frontend: login/register pages, auth Zustand store, Axios interceptor for JWT
- Update CORS config to expose `Authorization` header

## Capabilities

### New Capabilities
- `user-authentication`: JWT-based authentication with register, login, refresh token, and current user endpoints. Includes Spring Security integration, BCrypt password hashing, and protected route middleware.
- `auth-frontend`: Frontend login/register pages, JWT token management via Zustand, and Axios request interceptor for automatic token injection.

### Modified Capabilities
- `backend-scaffolding`: CORS config updated to expose Authorization header

## Impact

- **Backend**: New files in `config/`, `service/`, `service/impl/`, `controller/`, `dto/request/`, `dto/response/` packages
- **Dependencies**: Adding `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- **Frontend**: New pages, updated Zustand store, Axios interceptor
- **API**: New endpoints under `/api/auth/*`, all other endpoints become protected (require JWT)
- **Breaking**: `/api/health` will remain public; all future endpoints will require auth by default
