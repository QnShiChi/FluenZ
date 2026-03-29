## 1. Setup UI Components

- [x] 1.1 Add shadcn/ui Accordion component (`npx shadcn@latest add accordion`)
- [x] 1.2 Add shadcn/ui Sheet component (`npx shadcn@latest add sheet`)

## 2. Refactor AdminPage Layout

- [x] 2.1 Wrap the Topic mappings in an `<Accordion type="multiple">`
- [x] 2.2 Wrap each Topic item in `<AccordionItem>`, placing the Header in `<AccordionTrigger>` and Situations in `<AccordionContent>`
- [x] 2.3 Wrap the Situation mappings inside the Topic content in a nested `<Accordion>`
- [x] 2.4 Wrap each Situation in an `<AccordionItem>` holding its respective Chunks inside `<AccordionContent>`

## 3. Implement Sheet for Forms

- [x] 3.1 Wrap the Create/Edit Topic form into a `Sheet` component
- [x] 3.2 Wrap the Create/Edit Situation form into a `Sheet` component
- [x] 3.3 Wrap Chunk and SubPhrase edit forms into their respective Sheet components
- [x] 3.4 Wire up the "Sửa" and "Thêm mới" buttons in `AdminPage` to trigger the `open` state of the new Sheet components instead of showing inline forms

## 4. Testing & Polish

- [x] 4.1 Verify scroll position holds steady when opening Sheets and saving data
- [x] 4.2 Verify nested Accordions expand and collapse smoothly without visual formatting breakage
