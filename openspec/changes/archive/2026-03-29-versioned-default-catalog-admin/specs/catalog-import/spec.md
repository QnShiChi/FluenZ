# catalog-import (Delta Spec)

## ADDED

### Requirement: CSV import for full content tree
The system SHALL provide an admin-only CSV import flow that can create or update default catalog draft content across:
- topic
- situation
- chunk
- sub-phrase

#### Scenario: Import valid rows
- **WHEN** an admin uploads a CSV file containing valid rows
- **THEN** the system imports those rows into the target draft version

---

### Requirement: Partial success import behavior
The CSV import flow SHALL skip invalid rows while continuing to import valid rows from the same file.

#### Scenario: Mixed valid and invalid rows
- **WHEN** a CSV file contains both valid and invalid rows
- **THEN** valid rows are imported
- **AND** invalid rows are skipped
- **AND** the response includes row-level error details for each skipped row

---

### Requirement: Import preview/reporting
The admin import workflow SHALL expose a report summarizing import results before publish.

#### Scenario: Admin reviews import report
- **WHEN** an import finishes
- **THEN** the admin can review counts of imported rows, skipped rows, and detailed errors in the admin UI
