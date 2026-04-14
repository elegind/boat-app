import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection, signal } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let isAuthenticatedSignal: ReturnType<typeof signal<boolean>>;

  const fakeRoute = {} as ActivatedRouteSnapshot;
  const fakeState = {} as RouterStateSnapshot;

  beforeEach(() => {
    isAuthenticatedSignal = signal(false);

    authService = jasmine.createSpyObj<AuthService>([
      'login',
      'logout',
      'getAccessToken',
      'initAuth',
    ]);
    // Assign a real signal so isAuthenticated() behaves correctly
    (authService as unknown as Record<string, unknown>)['isAuthenticated'] =
      isAuthenticatedSignal.asReadonly();

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        { provide: AuthService, useValue: authService },
      ],
    });
  });

  it('should_returnTrue_when_userIsAuthenticated', () => {
    isAuthenticatedSignal.set(true);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(fakeRoute, fakeState),
    );

    expect(result).toBeTrue();
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('should_callLogin_and_returnFalse_when_notAuthenticated', () => {
    isAuthenticatedSignal.set(false);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(fakeRoute, fakeState),
    );

    expect(authService.login).toHaveBeenCalledOnceWith();
    expect(result).toBeFalse();
  });
});

