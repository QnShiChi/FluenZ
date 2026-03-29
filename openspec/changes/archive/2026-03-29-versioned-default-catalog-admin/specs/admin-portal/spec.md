# admin-portal (Delta Spec)

## ADDED

### Requirement: Separate admin workspace
The frontend SHALL provide a separate admin workspace for managing default catalog versions and content.

#### Scenario: Admin opens catalog workspace
- **WHEN** an authenticated admin navigates to the admin portal
- **THEN** the app shows admin-specific navigation and catalog management screens

---

### Requirement: In-app preview before publish
The admin portal SHALL allow admins to preview a draft default catalog version directly in the web interface before publishing.

#### Scenario: Preview draft version
- **WHEN** an admin opens preview for a draft version
- **THEN** the admin portal renders that draft version's topics, situations, chunks, and sub-phrases without making it live for learners

---

### Requirement: Publish reviewed version
The admin portal SHALL provide a publish action for reviewed draft versions.

#### Scenario: Publish from admin portal
- **WHEN** an admin publishes a reviewed draft version
- **THEN** the version becomes the active default version for future learner assignments
