## ADDED Requirements

### Requirement: Login page
The frontend SHALL provide a login page at `/login` with:
- Email and password input fields using Shadcn UI components
- Form validation (required fields, email format)
- Submit button that calls `POST /api/auth/login`
- Success: store tokens, redirect to home page
- Error: display error message
- Link to registration page

#### Scenario: Successful login
- **WHEN** a user enters valid credentials and submits
- **THEN** tokens are stored and the user is redirected to the home page

---

### Requirement: Registration page
The frontend SHALL provide a registration page at `/register` with:
- Fields: email, username, password, confirm password, current level (dropdown), goals (optional textarea)
- Form validation (required fields, email format, password match, minimum password length)
- Submit button that calls `POST /api/auth/register`
- Success: store tokens, redirect to home page
- Error: display error message (e.g., "Email already exists")
- Link to login page

#### Scenario: Successful registration
- **WHEN** a user fills in valid information and submits
- **THEN** the account is created, tokens are stored, and user is redirected

---

### Requirement: Auth state management with Zustand
The frontend SHALL manage authentication state via a Zustand store (`useAuthStore`) containing:
- `user`: current user object (or null)
- `accessToken`: JWT access token (or null)
- `refreshToken`: JWT refresh token (or null)
- `isAuthenticated`: computed boolean
- Actions: `login()`, `register()`, `logout()`, `refreshToken()`, `loadFromStorage()`

Tokens SHALL be persisted to `localStorage` and loaded on app initialization.

#### Scenario: Token persistence
- **WHEN** a user logs in and refreshes the browser
- **THEN** they remain logged in because tokens are restored from localStorage

---

### Requirement: Axios JWT interceptor
The frontend SHALL configure an Axios request interceptor that:
- Automatically adds `Authorization: Bearer <accessToken>` header to all API requests
- If a request returns 401, attempts to refresh the token and retry the request
- If refresh also fails, logs the user out and redirects to `/login`

#### Scenario: Automatic token injection
- **WHEN** any API request is made while the user is logged in
- **THEN** the access token is automatically included in the request header

---

### Requirement: Protected route wrapper
The frontend SHALL provide a `ProtectedRoute` component that:
- Checks if the user is authenticated
- If not, redirects to `/login`
- If yes, renders the child components

#### Scenario: Unauthenticated access
- **WHEN** an unauthenticated user navigates to a protected page
- **THEN** they are redirected to `/login`

---

### Requirement: React Router setup
The frontend SHALL configure React Router with routes:
- `/login` → Login page (public)
- `/register` → Registration page (public)
- `/` → Home page (protected)

#### Scenario: Route navigation
- **WHEN** the app loads
- **THEN** React Router handles navigation between login, register, and home pages
