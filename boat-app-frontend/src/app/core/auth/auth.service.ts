import { Injectable, inject, signal } from '@angular/core';
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

  /** reflects whether the current session holds a valid access token. */
  readonly isAuthenticated = signal<boolean>(false);

  readonly currentUser = signal<UserProfile | null>(null);

  /**
   * Initialises the OIDC client, tries to handle a redirect callback,
   * and sets up automatic token-refresh listeners.
   *
   * <p>Called once by {@code provideAppInitializer} before the first route is activated.
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
}

