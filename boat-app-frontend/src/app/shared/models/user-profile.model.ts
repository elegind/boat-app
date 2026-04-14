/**
 * minimal user profile returned by the Keycloak OIDC userinfo endpoint
 */
export interface UserProfile {
  sub: string;
  preferred_username: string;
}

