import { Injectable, signal } from '@angular/core';
import { EN_EN, type Translations } from '../../../i18n/en-EN';

/**
 * Signal-based translation service.
 *
 * Usage in any component:
 *   protected readonly t = inject(TranslationService).t;
 *   // template: {{ t().app.title }}
 *
 * Switching locale at runtime (future):
 *   inject(TranslationService).setLocale(FR_FR);
 */
@Injectable({ providedIn: 'root' })
export class TranslationService {
  private readonly _translations = signal<Translations>(EN_EN);

  /** Read-only signal — consume in components and templates. */
  readonly t = this._translations.asReadonly();

  /**
   * Switch to a different locale at runtime.
   * The locale object must satisfy the {@link Translations} type contract.
   */
  setLocale(locale: Translations): void {
    this._translations.set(locale);
  }
}

