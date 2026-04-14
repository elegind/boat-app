import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { Subject } from 'rxjs';
import { OAuthEvent, OAuthService, TokenResponse } from 'angular-oauth2-oidc';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let oauthServiceSpy: jasmine.SpyObj<OAuthService>;
  let eventSubject: Subject<OAuthEvent>;

  beforeEach(() => {
    eventSubject = new Subject<OAuthEvent>();

    oauthServiceSpy = jasmine.createSpyObj<OAuthService>('OAuthService', [
      'configure',
      'loadDiscoveryDocumentAndTryLogin',
      'hasValidAccessToken',
      'loadUserProfile',
      'initCodeFlow',
      'logOut',
      'getAccessToken',
      'refreshToken',
    ]);

    // Expose events as an Observable backed by the Subject
    (oauthServiceSpy as unknown as Record<string, unknown>)['events'] =
      eventSubject.asObservable();

    oauthServiceSpy.loadDiscoveryDocumentAndTryLogin.and.returnValue(
      Promise.resolve(true),
    );
    oauthServiceSpy.hasValidAccessToken.and.returnValue(false);
    oauthServiceSpy.getAccessToken.and.returnValue('');

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        AuthService,
        { provide: OAuthService, useValue: oauthServiceSpy },
      ],
    });

    service = TestBed.inject(AuthService);
  });

  it('should_setIsAuthenticated_true_when_validTokenExists', async () => {
    oauthServiceSpy.hasValidAccessToken.and.returnValue(true);
    oauthServiceSpy.loadUserProfile.and.returnValue(
      Promise.resolve({ info: { sub: '1', preferred_username: 'alice' } } as unknown as object),
    );

    await service.initAuth();

    expect(service.isAuthenticated()).toBeTrue();
  });

  it('should_setIsAuthenticated_false_when_noTokenExists', async () => {
    oauthServiceSpy.hasValidAccessToken.and.returnValue(false);

    await service.initAuth();

    expect(service.isAuthenticated()).toBeFalse();
  });


  it('should_callInitCodeFlow_when_loginCalled', () => {
    service.login();

    expect(oauthServiceSpy.initCodeFlow).toHaveBeenCalledOnceWith();
  });

  it('should_callLogOut_when_logoutCalled', () => {
    service.logout();

    expect(oauthServiceSpy.logOut).toHaveBeenCalledTimes(1);
  });

  // ── token events ─────────────────────────────────────────────────────────────

  it('should_callRefreshToken_when_tokenExpires', async () => {
    oauthServiceSpy.refreshToken.and.returnValue(
      Promise.resolve({} as unknown as TokenResponse),
    );

    await service.initAuth();
    eventSubject.next({ type: 'token_expires' } as OAuthEvent);

    expect(oauthServiceSpy.refreshToken).toHaveBeenCalledOnceWith();
  });

  it('should_callLogin_when_tokenRefreshErrorOccurs', async () => {
    await service.initAuth();
    eventSubject.next({ type: 'token_refresh_error' } as OAuthEvent);

    expect(oauthServiceSpy.initCodeFlow).toHaveBeenCalledOnceWith();
  });

  it('should_returnRoles_when_validTokenExists', () => {
    const payload = btoa(
      JSON.stringify({ realm_access: { roles: ['ROLE_ADMIN', 'ROLE_USER'] } }),
    );
    const fakeToken = `header.${payload}.signature`;
    oauthServiceSpy.getAccessToken.and.returnValue(fakeToken);

    const roles = service.getRoles();

    expect(roles).toContain('ROLE_ADMIN');
    expect(roles).toContain('ROLE_USER');
  });

  it('should_returnEmptyArray_when_noTokenExists', () => {
    oauthServiceSpy.getAccessToken.and.returnValue('');

    const roles = service.getRoles();

    expect(roles).toEqual([]);
  });

  it('should_returnTrue_for_isAdmin_when_tokenContainsRoleAdmin', async () => {
    const payload = btoa(
      JSON.stringify({ realm_access: { roles: ['ROLE_ADMIN'] } }),
    );
    const fakeToken = `header.${payload}.signature`;
    oauthServiceSpy.hasValidAccessToken.and.returnValue(true);
    oauthServiceSpy.getAccessToken.and.returnValue(fakeToken);
    oauthServiceSpy.loadUserProfile.and.returnValue(
      Promise.resolve({ info: { sub: '1', preferred_username: 'admin' } } as unknown as object),
    );

    await service.initAuth();

    expect(service.isAdmin()).toBeTrue();
  });

  it('should_returnFalse_for_isAdmin_when_tokenContainsOnlyRoleUser', async () => {
    const payload = btoa(
      JSON.stringify({ realm_access: { roles: ['ROLE_USER'] } }),
    );
    const fakeToken = `header.${payload}.signature`;
    oauthServiceSpy.hasValidAccessToken.and.returnValue(true);
    oauthServiceSpy.getAccessToken.and.returnValue(fakeToken);
    oauthServiceSpy.loadUserProfile.and.returnValue(
      Promise.resolve({ info: { sub: '2', preferred_username: 'user' } } as unknown as object),
    );

    await service.initAuth();

    expect(service.isAdmin()).toBeFalse();
  });
});



