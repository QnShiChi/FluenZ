## 1. Data Model and Security

- [x] 1.1 Add role support to users and secure admin-only backend routes
- [x] 1.2 Add learning mode selection and default catalog version assignment for users
- [x] 1.3 Introduce versioned default catalog entities for topic, situation, chunk, and sub-phrase content
- [x] 1.4 Add separate progress persistence for default catalog learning
- [x] 1.5 Add thumbnail/image fields for default situations and personalized situations

## 2. Default Catalog Backend

- [x] 2.1 Add APIs to fetch the active default catalog for learners based on their assigned version
- [x] 2.2 Add admin CRUD APIs for default catalog versions and nested content ordering
- [x] 2.3 Add draft preview and publish flows for default catalog versions
- [x] 2.4 Add learner mode-switch API to move between `DEFAULT` and `PERSONALIZED`
- [x] 2.5 Route users without a personalized path into onboarding when they switch modes

## 3. Media and Import

- [x] 3.1 Add local backend file upload and static serving for default catalog thumbnails/images
- [x] 3.2 Add CSV import endpoint for the full content tree into a draft version
- [x] 3.3 Return row-level import reports with skipped-row error details

## 4. Frontend Learner Experience

- [x] 4.1 Add learner dashboard mode switching between default and personalized content
- [x] 4.2 Render default catalog situations with thumbnails and version-aware content loading
- [x] 4.3 Preserve current personalized onboarding flow and redirect behavior when no personalized path exists

## 5. Admin Portal

- [x] 5.1 Add protected admin route space and admin layout
- [x] 5.2 Build default catalog version list and draft preview screens
- [x] 5.3 Build nested CRUD UI for topic, situation, chunk, and sub-phrase management
- [x] 5.4 Build image upload flows for situation thumbnails and sub-phrase images
- [x] 5.5 Build CSV import UI with validation summary and partial-success reporting
- [x] 5.6 Add publish action with safeguards so only reviewed draft versions become active for new users

## 6. Validation

- [x] 6.1 Add backend tests for role protection, version assignment, and publish behavior
- [x] 6.2 Add backend tests for CSV partial import and learner version retention
- [x] 6.3 Add frontend tests for learner mode switching and admin route protection
- [x] 6.4 Verify users on older default versions continue on their assigned version after a new publish
