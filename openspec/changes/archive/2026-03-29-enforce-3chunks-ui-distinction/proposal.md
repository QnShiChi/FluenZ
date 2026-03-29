## Why

Two issues reduce learning effectiveness:
1. **Inconsistent chunk count**: LLM sometimes generates 2 variableChunks instead of 3, breaking the practice flow structure.
2. **Poor visual distinction**: Situation detail page shows trunk and subphrases with similar styling, making it hard for users to understand the learning structure.

## What Changes

- **LLM Prompt**: Add strict rule enforcing exactly 3 variableChunks per chunk
- **Situation Detail UI**: Visually separate trunk (rootSentence) from subphrases with distinct labels and styling

## Capabilities

### Modified Capabilities
- `llm-path-generation`: Enforce 3 variableChunks rule
- `situation-detail-ui`: Trunk vs subphrase visual distinction

## Impact

- **Backend**: `LlmService.java` — prompt rule update
- **Frontend**: `SituationDetailPage.tsx` — UI restructure for trunk/subphrase sections
