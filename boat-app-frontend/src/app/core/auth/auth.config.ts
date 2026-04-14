import { AuthConfig } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';

/**
 * PKCE-based OAuth2 / OIDC configuration for Keycloak.
 */
export const authConfig: AuthConfig = {
  issuer: environment.keycloakIssuer,
  redirectUri: window.location.origin,
  clientId: 'boat-frontend',
  responseType: 'code',
  scope: 'openid profile',
  useSilentRefresh: false,
  sessionChecksEnabled: false,
  showDebugInformation: false,
  requireHttps: 'remoteOnly',
};

