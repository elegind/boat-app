import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Functional route guard — triggers the Keycloak PKCE login flow
 * when the user is not authenticated, then blocks navigation.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  if (authService.isAuthenticated()) {
    return true;
  }
  authService.login();
  return false;
};

