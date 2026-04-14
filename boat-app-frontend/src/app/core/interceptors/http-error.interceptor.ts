import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Functional HTTP error interceptor.
 * Logs all HTTP errors and re-throws them so feature services can handle them.
 * On 401 Unauthorized, triggers a PKCE re-login as a safety net
 * (e.g. token expired between the interceptor validity check and backend validation).
 */
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('HTTP Error:', error.status, error.message);
      if (error.status === 401) {
        authService.login();
      }
      return throwError(() => error);
    }),
  );
};
