import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

/**
 * Functional HTTP error interceptor.
 * Logs all HTTP errors and re-throws them so feature services can handle them.
 */
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('HTTP Error:', error.status, error.message);
      return throwError(() => error);
    })
  );
};
