## Overview

This change focuses on turning the dashboard situation card into a cleaner, more product-oriented card while keeping a premium editorial feel.

The target qualities are:

- premium
- clean
- easy to scan in 2 to 3 seconds
- light/dark mode friendly
- clearly action-oriented
- extensible for future progress/status states

The card should stop feeling like a visual showcase and instead behave like an efficient dashboard object.

## Structural Direction

The card should be organized into four clear vertical layers inside the content area:

1. meta row
2. title
3. short description
4. bottom action row

Overall composition:

- left column: image thumbnail
- right column: content area

Recommended internal component split:

- `LessonCard`
- `LessonCardMeta`
- `LessonCardContent`
- `LessonCardInfoPanel`
- `LessonCardCTA`

This split is encouraged if it improves maintainability and future extensibility.

## Layout Direction

### Desktop and large tablet

- Use a two-column card.
- The image column should occupy approximately `28%` to `34%` of the card width.
- The content column should occupy the remaining width.
- The content stack should remain visually dominant over the image.
- The CTA must remain within the first scan zone of the card, not pushed too far downward.

### Mobile

- The card may stack vertically when needed.
- The mobile version should preserve the same hierarchy: meta, title, description, then action row.
- The image should move above the content only when necessary.
- The CTA must remain prominent and easy to tap.

## Image Handling

- The image should be reduced in visual dominance by roughly `15%` to `20%` versus the current redesign.
- The image should retain large rounded corners and premium polish.
- The image should support the card visually without competing with title and CTA.
- The image should preserve important subjects better than a shallow banner crop.
- The image height should feel balanced with the whole card rather than reading as the primary attraction.

## Meta Row

All metadata should live in one compact row near the top of the content area.

Meta row may include:

- level badge
- phrase count
- optional estimated time if it helps the product

Requirements:

- phrase count should no longer be isolated as a standalone widget box
- metadata should look unified, compact, and scannable
- metadata should not overpower the title

## Content Hierarchy

The content hierarchy must be:

1. CTA as the strongest actionable signal
2. title as the strongest informational signal
3. description as short support copy
4. meta row as compact contextual support

Requirements:

- title should be bold, clean, and easy to read
- description should be concise and easy to scan
- spacing between meta, title, and description should feel rhythmic and breathable
- avoid fragmenting text with too many independent inner cards

## Bottom Action Row

The bottom row should contain two zones.

### A. Info/Support Panel

This panel explains the learning method, for example:

- start from smaller phrases
- then combine into a complete sentence
- understand the context before speaking

Requirements:

- lighter visual weight than the CTA
- soft styling
- minimal “card inside card” feeling
- supportive, not dominant

### B. Primary CTA Panel

This is the most important element in the card.

Requirements:

- “Vào học ngay” must be the clearest focal action
- obvious click/tap affordance
- should stand out more than the support panel
- should feel like the primary next step, not just another block

## Visual Weight Reduction

The redesign should reduce the heavy layered feeling of the current card.

Guidelines:

- minimize unnecessary nested surfaces
- reduce the number of equally emphasized blocks
- use layout, spacing, and typography as the main hierarchy tools
- keep enough breathing room without making the card feel empty

## Border, Radius, Elevation

Refine:

- outer card
- inner panels
- badges/chips
- CTA block

Goals:

- consistent corner radius language
- less widget fragmentation
- hierarchy created by emphasis and placement, not by putting everything in a separate box

## Interaction and Extensibility

- Whole-card hover should remain subtle and premium.
- CTA hover/focus should be more explicit than whole-card hover.
- The structure should remain flexible enough to add future states such as `completed`, `in-progress`, or progress percentage without breaking layout.

## Risks

- If the CTA and info panel are still styled too similarly, the primary action will remain diluted.
- If the image remains too large, the card will continue to feel showcase-heavy.
- If metadata is not unified cleanly, the header will still feel fragmented.

## Success Criteria

- Situation cards feel premium but product-focused.
- Users can understand the card within 2 to 3 seconds.
- CTA is clearly the strongest actionable element.
- Metadata feels compact and unified.
- Light mode and dark mode both preserve readability and hierarchy.
- The structure is clean enough for future status and progress extensions.
