## ADDED Requirements

### Requirement: Spring Security with JWT filter chain
The system SHALL configure Spring Security to use a stateless JWT authentication filter. The `SecurityFilterChain` SHALL:
- Disable CSRF (stateless API)
- Set session management to STATELESS
- Permit public access to: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `GET /api/health`
- Require authentication for all other endpoints
- Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`

#### Scenario: Public endpoints accessible without token
- **WHEN** a request is made to `/api/auth/login` without an Authorization header
- **THEN** the request is processed normally (not blocked by security)

#### Scenario: Protected endpoints require token
- **WHEN** a request is made to `/api/auth/me` without a valid JWT
- **THEN** the response status is 401 Unauthorized

---

### Requirement: JWT token generation and validation
The system SHALL provide a `JwtService` that:
- Generates access tokens (24h expiry) containing user email as subject
- Generates refresh tokens (7 days expiry)
- Validates tokens (signature, expiry)
- Extracts user email from tokens
- Uses HMAC-SHA256 signing with a secret from environment variable `JWT_SECRET`

#### Scenario: Valid token accepted
- **WHEN** a request includes a valid, non-expired JWT in the Authorization header
- **THEN** the user is authenticated and the request proceeds

#### Scenario: Expired token rejected
- **WHEN** a request includes an expired JWT
- **THEN** the response is 401 Unauthorized

---

### Requirement: User registration endpoint
The system SHALL provide `POST /api/auth/register` that:
- Accepts: email, username, password, currentLevel, goals
- Hashes the password using BCrypt before storing
- Returns 201 Created with access token, refresh token, and user info
- Returns 409 Conflict if email or username already exists

#### Scenario: Successful registration
- **WHEN** a valid registration request is sent with a unique email and username
- **THEN** the user is created, password is hashed, and JWT tokens are returned

#### Scenario: Duplicate email
- **WHEN** a registration request is sent with an already-registered email
- **THEN** the response is 409 Conflict with an error message

---

### Requirement: User login endpoint
The system SHALL provide `POST /api/auth/login` that:
- Accepts: email and password
- Validates credentials against stored BCrypt hash
- Returns 200 OK with access token, refresh token, and user info
- Returns 401 Unauthorized if credentials are invalid

#### Scenario: Successful login
- **WHEN** valid email and password are provided
- **THEN** JWT tokens and user info are returned

#### Scenario: Invalid credentials
- **WHEN** wrong password is provided
- **THEN** the response is 401 Unauthorized

---

### Requirement: Token refresh endpoint
The system SHALL provide `POST /api/auth/refresh` that:
- Accepts: a refresh token in the request body
- Validates the refresh token
- Returns a new access token (and optionally a new refresh token)
- Returns 401 if the refresh token is invalid or expired

#### Scenario: Token refreshed
- **WHEN** a valid refresh token is sent
- **THEN** a new access token is returned

---

### Requirement: Current user endpoint
The system SHALL provide `GET /api/auth/me` (protected) that:
- Returns the authenticated user's profile (id, email, username, level, goals, createdAt)
- Does NOT include passwordHash

#### Scenario: Get current user
- **WHEN** an authenticated user calls `/api/auth/me`
- **THEN** their profile is returned without sensitive fields

---

### Requirement: BCrypt password encoding
The system SHALL use `BCryptPasswordEncoder` (strength 10) as the `PasswordEncoder` bean. All user passwords SHALL be hashed before storage and verified during login.

#### Scenario: Password stored as hash
- **WHEN** a user registers with password "myPassword123"
- **THEN** the database stores a BCrypt hash, NOT the plain text

---

### Requirement: JWT environment configuration
The system SHALL read JWT configuration from environment variables:
- `JWT_SECRET`: Signing key (minimum 256-bit / 32 characters)
- `JWT_ACCESS_EXPIRATION`: Access token expiry in milliseconds (default: 86400000 = 24h)
- `JWT_REFRESH_EXPIRATION`: Refresh token expiry in milliseconds (default: 604800000 = 7 days)

These SHALL be added to `.env`, `.env.example`, and `application.yml`.

#### Scenario: JWT secret from environment
- **WHEN** the backend starts
- **THEN** it reads the JWT secret from the `JWT_SECRET` environment variable
