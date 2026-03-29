# user-authentication (Delta Spec)

## MODIFIED

- **REQ-SPRING-SECURITY-JWT-FILTER-CHAIN**: The security filter chain SHALL require authenticated access for learner APIs and SHALL additionally enforce admin-only access for default catalog management, import, upload, preview, and publish endpoints.
- **REQ-CURRENT-USER-ENDPOINT**: The current user profile response SHALL include the user's role and preferred learning mode.

## ADDED

### Requirement: Role-based authorization
The system SHALL authorize users by role, with at least `USER` and `ADMIN`.

#### Scenario: Admin endpoint denied to learner
- **WHEN** an authenticated non-admin user requests an admin-only catalog management endpoint
- **THEN** the response is 403 Forbidden

#### Scenario: Admin endpoint allowed to admin
- **WHEN** an authenticated admin requests an admin-only catalog management endpoint
- **THEN** the request is authorized if all other validations pass

---

### Requirement: Learning mode update endpoint
The system SHALL provide a protected endpoint allowing a learner to switch their preferred learning mode between `DEFAULT` and `PERSONALIZED`.

#### Scenario: Switch to default mode
- **WHEN** a learner updates their preferred learning mode to `DEFAULT`
- **THEN** the system stores `DEFAULT` as the preferred mode

#### Scenario: Switch to personalized mode with existing path
- **WHEN** a learner updates their preferred learning mode to `PERSONALIZED` and already has a personalized path
- **THEN** the system stores `PERSONALIZED` as the preferred mode

#### Scenario: Switch to personalized mode without path
- **WHEN** a learner updates their preferred learning mode to `PERSONALIZED` and has no active personalized path
- **THEN** the response indicates onboarding is required before the learner can enter personalized mode
