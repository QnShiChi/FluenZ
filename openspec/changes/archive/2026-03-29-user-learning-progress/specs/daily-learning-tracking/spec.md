## ADDED Requirements

### Requirement: Daily Goal Tracking
The system MUST maintain daily records of the user's learning duration and evaluate whether the daily goal of 5 minutes is met.

#### Scenario: Advancing learning duration
- **WHEN** a user completes a learning chunk
- **THEN** the system MUST add a fixed amount (e.g., 3 minutes) to the user's `total_learning_minutes` for the current day
- **AND** if the total time reaches or exceeds 5 minutes, the current day `is_goal_reached` MUST be marked as true

### Requirement: Learning Streak Calculation
The system MUST increment the user's consecutive daily streak whenever the daily 5-minute goal is reached.

#### Scenario: Reaching daily goal to extend streak
- **WHEN** the daily goal is reached on a learning session
- **THEN** if the user also reached the goal yesterday, the `current_streak` MUST increase by 1
- **AND** if the `current_streak` exceeds `longest_streak`, the `longest_streak` MUST be updated
- **AND** if the user failed to reach the goal yesterday, the `current_streak` MUST reset to 1

### Requirement: Spoken Times Tracking
The system MUST track the number of times a user engages their microphone for spoken responses.

#### Scenario: Submitting a voice response
- **WHEN** a user successfully submits a voice audio recording during a speaking exercise or roleplay
- **THEN** the backend MUST increment the `total_spoken_count` metric on the user's overarching stats profile
