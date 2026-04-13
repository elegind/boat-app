---
name: angular-best-practices
description: Apply Angular 20 best practices — standalone components,
  signals, TypeScript strict, i18n, mobile-first responsive design.
  Use when writing or reviewing any Angular code.
  Triggers on component, Angular, TypeScript, template, i18n, label.
allowed-tools: ["read", "edit", "search/codebase"]
---

# Angular 20 — best practices

## Component design
- One component = one responsibility — split if it does two things
- Always prefer smaller reusable components:
  BoatListComponent
  └── BoatCardComponent
  └── BoatStatusBadgeComponent
- Standalone always — never add to NgModules
- OnPush change detection on every component
- Max 150 lines TypeScript, max 50 lines HTML template
- Import only what the component needs in its imports: []

## Signals — use over RxJS for all local state
- State:   signal()
- Derived: computed()
- Effects: effect() — sparingly, prefer computed()
- Never use BehaviorSubject for component-level state
- Services sharing state across components use signal() too
- Only use Observable for HTTP calls (Angular HttpClient)

## TypeScript (strict — non-negotiable)
- No any — use unknown and narrow with type guards
- No non-null assertion (!) — use optional chaining (?.) instead
- Use the new input()/output() function API — never @Input()/@Output():
  title = input.required<string>();   ← required, signal-based
  subtitle = input<string>('');       ← optional with default
  boatSelected = output<Boat>();      ← replaces EventEmitter
- Explicit return types on all public methods
- Interfaces for all data models — place in shared/models/

## i18n — non-negotiable rule
Never hardcode any user-visible text in templates or TypeScript.
Every label, button, title, placeholder, error must be in:
src/i18n/en-EN.ts  (TypeScript const, NOT JSON)

Structure by feature — nested objects matching component hierarchy:
```typescript
export const EN_EN = {
  common: { save: 'Save', cancel: 'Cancel' },
  boats: { list: { title: 'My Boats' }, form: { name: 'Boat name' } }
};
```

Inject `TranslationService` in every component that needs labels:
```typescript
protected readonly t = inject(TranslationService).t;
```

Usage:
Template:    {{ t().boats.list.title }}
TypeScript:  this.t().common.save

Never use a `translate` pipe — the project uses a signal-based
TranslationService, not ngx-translate.

## Mobile-first responsive (mandatory)
- Design at 320px first — then scale up with Tailwind prefixes
- Never use fixed pixel widths — use w-full, max-w-*, %
- Minimum tap target: 48x48px on all interactive elements
- Breakpoints to verify: 320px, 375px, 768px, 1024px

## SCSS
- Component SCSS for component-specific styles only
- Never use ::ng-deep — fix ViewEncapsulation properly
- Prefer Tailwind utilities over custom SCSS whenever possible