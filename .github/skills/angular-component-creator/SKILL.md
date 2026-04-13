---
name: angular-component-creator
description: Create a new Angular 20 standalone component following all
  project conventions. Use when asked to create any component, page
  or UI element.
allowed-tools: ["read", "edit", "search/codebase"]
---

# Creating an Angular component

## Step 1 — Decide location
- Reusable across multiple features → shared/components/[name]/
- Specific to one feature           → features/[feature]/components/[name]/
- New page/route                    → features/[name]/[name].component.*

## Step 2 — Create the three files
[name].component.ts
[name].component.html
[name].component.scss

## Step 3 — TypeScript structure (always in this order)
@Component({
selector: 'app-[name]',
standalone: true,
changeDetection: ChangeDetectionStrategy.OnPush,
imports: [ /* only what this component uses */ ],
templateUrl: './[name].component.html',
styleUrl: './[name].component.scss'
})
export class [Name]Component {
// 1. Inputs  — input() / input.required()
// 2. Outputs — output()
// 3. Services — inject()
// 4. State   — signal()
// 5. Derived — computed()
// 6. Methods
}

## Step 4 — Add translation keys first
Before writing any template HTML, add all required keys to
src/i18n/en-EN.ts  (TypeScript const — NOT a JSON file).

Add new keys under the matching feature namespace:
```typescript
export const EN_EN = {
  // ...existing keys...
  featureName: {
    someLabel: 'My label',
  },
};
```

Then inject `TranslationService` in the component:
```typescript
protected readonly t = inject(TranslationService).t;
```

And reference keys in the template:
```html
{{ t().featureName.someLabel }}
```

Never write a label, title or button text without a translation key.
Never use the `translate` pipe — use `t()` signal accessor only.

## Step 5 — Write mobile-first HTML
Start the layout at 320px, add responsive classes for larger screens.
Use Angular Material components for UI elements.
Use Tailwind for layout, spacing and responsive utilities.

## Checklist before declaring done
- [ ] ChangeDetectionStrategy.OnPush set
- [ ] All text uses translation keys — nothing hardcoded
- [ ] Layout verified at 320px and 768px
- [ ] All interactive elements min 48x48px tap target
- [ ] No any, no ! in TypeScript
- [ ] Component added to parent imports if used immediately
- [ ] New route added to app.routes.ts with loadComponent if it's a page