## 1. Card Structure

- [ ] 1.1 Refactor the situation card into a cleaner two-column structure with image thumbnail on the left and content area on the right
- [ ] 1.2 Rebuild the content flow into `meta row -> title -> short description -> bottom action row`
- [ ] 1.3 Reduce unnecessary nested card layers so the card feels lighter and more product-oriented
- [ ] 1.4 Split the implementation into reusable subcomponents if that improves maintainability (`LessonCard`, `LessonCardMeta`, `LessonCardContent`, `LessonCardInfoPanel`, `LessonCardCTA`)

## 2. Image Treatment

- [ ] 2.1 Reduce image dominance by roughly 15 to 20 percent compared with the current redesign
- [ ] 2.2 Keep generous radius and premium polish while ensuring the image supports rather than dominates the card
- [ ] 2.3 Tune image framing for people-centered imagery so important focal areas are less likely to be cropped awkwardly

## 3. Metadata And Hierarchy

- [ ] 3.1 Consolidate level badge and phrase count into a single compact meta row
- [ ] 3.2 Remove the standalone phrase-count widget treatment
- [ ] 3.3 Strengthen title hierarchy and tighten spacing between meta, title, and description
- [ ] 3.4 Keep description concise and easy to scan in both light and dark mode

## 4. Bottom Action Row

- [ ] 4.1 Build a lighter support/info panel explaining the learning method
- [ ] 4.2 Build a stronger primary CTA panel where `Vào học ngay` is the most visually prominent action
- [ ] 4.3 Ensure CTA affordance is clear for hover, focus, click, and tap states
- [ ] 4.4 Make sure the support panel does not compete visually with the CTA panel

## 5. Responsive And Theme Behavior

- [ ] 5.1 Preserve a strong two-column hierarchy on desktop and larger tablet widths
- [ ] 5.2 Keep text readable and layout balanced on smaller desktops and tablets
- [ ] 5.3 Add a clean stacked mobile fallback where image moves above content only when needed
- [ ] 5.4 Verify the card works well in both light mode and dark mode

## 6. Extensibility And Verification

- [ ] 6.1 Keep the structure easy to extend with future progress, completion, or in-progress states
- [ ] 6.2 Verify cards with people-centered imagery
- [ ] 6.3 Verify cards with landscape-style imagery
- [ ] 6.4 Verify `/dashboard` scanning clarity and CTA prominence at desktop, tablet, and mobile widths
