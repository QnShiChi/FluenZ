## ADDED Requirements

### Requirement: Multi-provider image fetching
The system MUST support fetching stock images from multiple providers with automatic fallback when a provider fails or returns no results.

#### Scenario: Primary provider returns a result
- **WHEN** an image is requested for a given keyword
- **AND** the keyword is not in the cache
- **THEN** the system MUST query Unsplash first
- **AND** if Unsplash returns a valid image URL, the system MUST use that URL
- **AND** the system MUST store the keyword→URL mapping in the cache

#### Scenario: Primary provider fails, fallback succeeds
- **WHEN** an image is requested for a given keyword
- **AND** Unsplash returns no result or errors
- **THEN** the system MUST try Pexels as the second provider
- **AND** if Pexels fails, the system MUST try Pixabay as the third provider
- **AND** the first successful result MUST be cached and returned

#### Scenario: All providers fail
- **WHEN** all three providers fail or return no results for a keyword
- **THEN** the system MUST return `null` gracefully without throwing an exception
- **AND** the system MUST NOT cache a `null` result

#### Scenario: Provider API key not configured
- **WHEN** a provider's API key is blank or missing
- **THEN** the system MUST skip that provider and proceed to the next one in the chain
- **AND** the system MUST log a debug message indicating the provider was skipped

### Requirement: Keyword-based image cache
The system MUST maintain a persistent cache mapping search keywords to resolved image URLs to avoid redundant external API calls.

#### Scenario: Cache hit
- **WHEN** an image is requested for a keyword that already exists in the cache
- **AND** the cached entry has not expired
- **THEN** the system MUST return the cached URL without calling any external provider

#### Scenario: Cache miss
- **WHEN** an image is requested for a keyword not present in the cache
- **THEN** the system MUST query the provider chain
- **AND** if a result is found, the system MUST store it in the cache with a 30-day expiry

#### Scenario: Concurrent cache writes for the same keyword
- **WHEN** two requests simultaneously attempt to cache the same keyword
- **THEN** the system MUST handle the conflict gracefully using upsert semantics
- **AND** the system MUST NOT throw a duplicate key exception
