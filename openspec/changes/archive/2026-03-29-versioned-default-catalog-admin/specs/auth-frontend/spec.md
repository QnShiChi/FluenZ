# auth-frontend (Delta Spec)

## MODIFIED

- **REQ-AUTH-STATE-MANAGEMENT-WITH-ZUSTAND**: The auth store SHALL persist the authenticated user's role and preferred learning mode in addition to token state.
- **REQ-PROTECTED-ROUTE-WRAPPER**: The frontend SHALL support role-aware protected routes for learner pages and admin pages.

## ADDED

### Requirement: Admin route protection
The frontend SHALL provide route protection for admin pages that only renders admin UI to authenticated users with the `ADMIN` role.

#### Scenario: Learner blocked from admin route
- **WHEN** an authenticated non-admin user navigates to an admin route
- **THEN** the app redirects them away from the admin area or shows an unauthorized view

#### Scenario: Admin allowed into admin route
- **WHEN** an authenticated admin user navigates to an admin route
- **THEN** the admin page is rendered

---

### Requirement: Learning mode-aware app bootstrap
The frontend SHALL initialize the learner experience using the authenticated user's preferred learning mode.

#### Scenario: Default-mode learner enters app
- **WHEN** an authenticated learner with preferred mode `DEFAULT` opens the app
- **THEN** the learner experience loads default catalog content first

#### Scenario: Personalized-mode learner without path
- **WHEN** an authenticated learner with preferred mode `PERSONALIZED` opens the app and has no active personalized path
- **THEN** the app redirects the learner to onboarding
