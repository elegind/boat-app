import { Injectable, signal } from '@angular/core';

/**
 * Fake authentication service — no real token or persistence.
 *
 * A call to {@link login} simply flips the in-memory signal to `true`.
 * The state resets to `false` on every page refresh, which is intentional
 * for this placeholder implementation.
 *
 * Real OAuth / token-based integration will replace this service later.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _isAuthenticated = signal(false);

  /** Read-only signal — consume in guards and components. */
  readonly isAuthenticated = this._isAuthenticated.asReadonly();

  /** Mark the current session as authenticated. */
  login(): void {
    this._isAuthenticated.set(true);
  }

  /** Clear the current session. */
  logout(): void {
    this._isAuthenticated.set(false);
  }
}

