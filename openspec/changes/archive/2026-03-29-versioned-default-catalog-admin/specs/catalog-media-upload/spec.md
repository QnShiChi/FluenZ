# catalog-media-upload (Delta Spec)

## ADDED

### Requirement: Local media upload for default catalog
The system SHALL provide admin-only upload endpoints for default-catalog situation thumbnails and sub-phrase images, storing files on the backend server.

#### Scenario: Upload default situation thumbnail
- **WHEN** an admin uploads an image for a default situation
- **THEN** the backend stores the file locally
- **AND** associates the stored file path or served URL with that default situation

#### Scenario: Upload default sub-phrase image
- **WHEN** an admin uploads an image for a default sub-phrase
- **THEN** the backend stores the file locally
- **AND** associates the stored file path or served URL with that default sub-phrase

---

### Requirement: Learner-consumable media URLs
The system SHALL return default-catalog media paths in learner and preview responses so the frontend can render thumbnails and phrase imagery.

#### Scenario: Render uploaded thumbnail
- **WHEN** a learner or admin preview client requests a default situation that has an uploaded thumbnail
- **THEN** the response includes a media URL that the frontend can render
