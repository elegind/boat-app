import { Injectable, computed, inject, signal } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { filter } from 'rxjs';
import { authConfig } from './auth.config';
import { UserProfile } from '../../shared/models/user-profile.model';

/**
 * OAuth2 PKCE authentication service backed by Keycloak via angular-oauth2-oidc.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly oauthService = inject(OAuthService);

  /** Reflects whether the current session holds a valid access token. */
  readonly isAuthenticated = signal<boolean>(false);

  readonly currentUser = signal<UserProfile | null>(null);

  readonly isAdmin = computed(
    () => this.isAuthenticated() && this.getRoles().includes('ROLE_ADMIN'),
  );

  /**
   * Initialises the OIDC client, tries to handle a redirect callback,
   * and sets up automatic token-refresh listeners.
   *
   * Called once by {@code provideAppInitializer} before the first route is activated.
   */
  async initAuth(): Promise<void> {
    this.oauthService.configure(authConfig);
    await this.oauthService.loadDiscoveryDocumentAndTryLogin();

    this.isAuthenticated.set(this.oauthService.hasValidAccessToken());

    if (this.isAuthenticated()) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const profile = (await this.oauthService.loadUserProfile()) as any;
      this.currentUser.set(profile.info as UserProfile);
    }

    // Proactively refresh the access token before it expires.
    this.oauthService.events
      .pipe(filter((e) => e.type === 'token_expires'))
      .subscribe(() => {
        this.oauthService.refreshToken().catch(() => this.login());
      });

    // Fall back to a full re-login if the refresh itself fails.
    this.oauthService.events
      .pipe(filter((e) => e.type === 'token_refresh_error'))
      .subscribe(() => this.login());
  }

  login(): void {
    this.oauthService.initCodeFlow();
  }

  logout(): void {
    this.oauthService.logOut();
  }

  getAccessToken(): string {
    return this.oauthService.getAccessToken();
  }

  /**
   * Decodes the current access token and returns the realm roles from
   * the {@code realm_access.roles} claim.
   *
   * @returns an array of role strings, or an empty array if no token is present
   */
  getRoles(): string[] {
    const token = this.oauthService.getAccessToken();
    if (!token) return [];
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      // eslint-disable-next-line @typescript-eslint/no-unsafe-return, @typescript-eslint/no-unsafe-member-access
      return payload?.realm_access?.roles ?? [];
    } catch {
      return [];
    }
  }
}
