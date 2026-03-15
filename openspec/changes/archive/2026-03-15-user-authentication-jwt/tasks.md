## 1. Backend Dependencies & Configuration

- [x] 1.1 Add Spring Security, JJWT dependencies to `build.gradle.kts`
- [x] 1.2 Add JWT environment variables to `.env`, `.env.example`, and `application.yml`
- [x] 1.3 Create `BCryptPasswordEncoder` bean in config

## 2. JWT Service

- [x] 2.1 Create `JwtService` — generate access token, generate refresh token, validate token, extract email
- [x] 2.2 Create `CustomUserDetailsService` implementing `UserDetailsService` to load user by email

## 3. Security Configuration

- [x] 3.1 Create `JwtAuthenticationFilter` extending `OncePerRequestFilter`
- [x] 3.2 Create `SecurityConfig` with `SecurityFilterChain` (public/protected endpoints, stateless session, JWT filter)
- [x] 3.3 Update `CorsConfig` to expose `Authorization` header

## 4. Auth DTOs

- [x] 4.1 Create `LoginRequest` (email, password)
- [x] 4.2 Create `RegisterRequest` (email, username, password, currentLevel, goals)
- [x] 4.3 Create `AuthResponse` (accessToken, refreshToken, user info)
- [x] 4.4 Create `RefreshTokenRequest` (refreshToken)

## 5. Auth Service & Controller

- [x] 5.1 Create `AuthService` interface with register, login, refresh methods
- [x] 5.2 Create `AuthServiceImpl` with BCrypt hashing, JWT generation, credential validation
- [x] 5.3 Create `AuthController` with POST /register, POST /login, POST /refresh, GET /me endpoints

## 6. Frontend Dependencies

- [x] 6.1 Install `react-router-dom` in frontend
- [x] 6.2 Configure React Router in `App.tsx` with routes for login, register, and home

## 7. Frontend Auth

- [x] 7.1 Create `useAuthStore` Zustand store with login/register/logout/refresh actions and localStorage persistence
- [x] 7.2 Update `src/services/api.ts` Axios interceptor to inject JWT and handle 401 refresh
- [x] 7.3 Create `ProtectedRoute` component

## 8. Frontend Pages

- [x] 8.1 Create Login page with Shadcn UI form components
- [x] 8.2 Create Register page with Shadcn UI form components
- [x] 8.3 Update Home page to show user info and logout button

## 9. Verification

- [x] 9.1 Rebuild backend Docker image and verify startup
- [x] 9.2 Test register → login → /me → refresh flow via curl
- [x] 9.3 Test protected endpoint returns 401 without token
- [x] 9.4 Verify frontend login/register flow in browser
