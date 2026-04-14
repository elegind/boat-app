import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

/**
 * attaches the Bearer access token
 * to every request targeting the backend API URL.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();

  if (token && req.url.startsWith(environment.apiUrl)) {
    return next(
      req.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
      }),
    );
  }

  return next(req);
};

