import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';
import { environment } from '../../../environments/environment';

describe('authInterceptor', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let httpMock: HttpTestingController;
  let http: HttpClient;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>(['getAccessToken']);

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    http = TestBed.inject(HttpClient);
  });

  afterEach(() => httpMock.verify());

  it('should_addBearerToken_when_requestTargetsApiUrl', () => {
    authService.getAccessToken.and.returnValue('my-access-token');

    http.get(`${environment.apiUrl}/boats`).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/boats`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-access-token');
    req.flush([]);
  });

  it('should_notAddBearerToken_when_requestTargetsExternalUrl', () => {
    authService.getAccessToken.and.returnValue('my-access-token');

    http.get('https://external.example.com/data').subscribe();

    const req = httpMock.expectOne('https://external.example.com/data');
    expect(req.request.headers.get('Authorization')).toBeNull();
    req.flush({});
  });

  it('should_notAddBearerToken_when_noTokenAvailable', () => {
    authService.getAccessToken.and.returnValue('');

    http.get(`${environment.apiUrl}/boats`).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/boats`);
    expect(req.request.headers.get('Authorization')).toBeNull();
    req.flush([]);
  });
});

