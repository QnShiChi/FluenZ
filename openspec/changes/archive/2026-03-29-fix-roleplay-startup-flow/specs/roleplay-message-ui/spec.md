## ADDED Requirements

### Requirement: Isolated Evaluation UI Block
The system MUST render the AI's final English response and the Vietnamese evaluation/feedback as two entirely separate chat message blocks in the UI.

#### Scenario: Rendering the final AI turn
- **WHEN** the AI returns its 3rd and final turn containing both an answer and an evaluation
- **THEN** the UI MUST render the English answer in one chat bubble modeled as `role: "ai", tone: "default"`
- **AND** the UI MUST render the Vietnamese evaluation in a completely separate, visually distinct chat bubble modeled as `role: "ai", tone: "evaluation"`

### Requirement: Robust Message Split Fallback
The system MUST securely split the English text from the Vietnamese evaluation text even if the LLM fails to output the exact `---` separator.

#### Scenario: Splitting text without standard separator
- **WHEN** the LLM response is received without a `---` separator but contains standard Vietnamese evaluation keywords (e.g., "Danh gia:", "Đánh giá:")
- **THEN** the system MUST fallback to splitting the message at the evaluation keyword, reliably separating the English response from the Vietnamese feedback
