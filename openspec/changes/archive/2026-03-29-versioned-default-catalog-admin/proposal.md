## Why

FluenZ currently supports only one active learning path per user, generated through onboarding and AI. That model does not support a shared default curriculum, admin-managed content operations, versioned publishing, or local thumbnail management. We need to introduce a stable default learning catalog that all users can start with, while preserving the existing personalized AI path as a separate mode with separate progress.

## What Changes

- Add a versioned `DEFAULT` catalog managed entirely by admins, separate from user-specific `PERSONALIZED` learning paths.
- Introduce admin roles/permissions and a dedicated admin web portal for managing the full content tree: Topic -> Situation -> Chunk -> SubPhrase.
- Add draft/preview/publish workflow for default catalog versions so admins can review content in the admin UI before release.
- Allow CSV import for the full content tree with partial success behavior: valid rows import, invalid rows are skipped and reported.
- Add local image upload and storage in the backend for default catalog thumbnails and sub-phrase images.
- Add situation thumbnails to the learning experience; personalized paths continue using AI plus Unsplash image lookup for both situation and sub-phrase imagery.
- Add user learning mode switching between `DEFAULT` and `PERSONALIZED`, with separate progress tracking for each mode.
- Route users to the default experience by default; when switching to personalized mode without an active personalized path, redirect them into onboarding.
- Preserve users on the default catalog version they started until they finish, even after a newer default version is published.

## Capabilities

### New Capabilities
- `default-learning-catalog`: versioned shared curriculum with draft, preview, and publish lifecycle
- `admin-content-management`: admin-only CRUD and ordering for topic, situation, chunk, and sub-phrase content
- `catalog-import`: CSV import with row-level validation, partial success, and error reporting
- `catalog-media-upload`: backend-hosted image upload for default catalog thumbnails and phrase visuals
- `admin-portal`: separate admin web interface for previewing and publishing catalog versions

### Modified Capabilities
- `data-model`: split shared default catalog content from personalized learning paths; add roles, learning mode selection, version assignment, thumbnails, and separate progress ownership
- `user-authentication`: support admin authorization and route protection for admin-only APIs
- `auth-frontend`: support admin-aware session handling and admin app entry points

## Impact

- **Database**: new catalog/versioning structures, role fields, mode fields, user-to-version assignment, separate progress tracking, and image metadata
- **Backend**: new admin APIs, upload/import flows, publish/version services, and adjusted learning-mode resolution
- **Frontend**: new admin portal, dashboard mode switch, default catalog presentation, and richer situation cards with thumbnails
- **AI flow**: personalized onboarding remains, but image generation expands to situation-level imagery in addition to sub-phrase imagery
