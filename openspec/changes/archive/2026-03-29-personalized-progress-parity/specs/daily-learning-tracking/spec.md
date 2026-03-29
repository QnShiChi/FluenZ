## MODIFIED Requirements

### Requirement: Daily Goal Tracking
The system MUST maintain daily records of the user's learning duration and evaluate whether the daily goal of 5 minutes is met.

#### Scenario: Advancing learning duration in personalized mode
- **WHEN** a user completes a learning chunk in `PERSONALIZED` mode
- **AND** the completion request includes the chunk session's `totalTimeSeconds`
- **THEN** the system MUST convert that realtime duration into gained learning minutes using the same rules as `DEFAULT` mode
- **AND** the system MUST cap the gained time for that completion at `10 minutes`
- **AND** if the reported duration is greater than `0` seconds but less than `60` seconds, the system MUST award `1 minute`
- **AND** if the total time for the day reaches or exceeds `5 minutes`, the current day `is_goal_reached` MUST be marked as true

#### Scenario: Replaying a completed personalized chunk
- **WHEN** a user completes a personalized chunk that was already marked completed in the past
- **THEN** the system MUST still add realtime learning minutes for the new session
- **AND** the system MUST NOT require the personalized chunk to be newly completed in order to count time

### Requirement: Learning Streak Calculation
The system MUST increment the user's consecutive daily streak whenever the daily 5-minute goal is reached.

#### Scenario: Reaching daily goal from personalized learning
- **WHEN** the daily goal is reached through a personalized learning session
- **THEN** streak and longest streak MUST be updated using the same rules as default learning sessions
