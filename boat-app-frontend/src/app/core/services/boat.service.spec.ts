import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { BoatService } from './boat.service';
import { environment } from '../../../environments/environment';

describe('BoatService', () => {
  let service: BoatService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        BoatService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(BoatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  const emptyPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 5 };

  // ── Happy path ─────────────────────────────────────────────────────────────

  it('getBoats_should_callGetWithCorrectUrl_when_defaultParams', () => {
    service.getBoats(0, 5).subscribe();
    const req = httpMock.expectOne(
      (r) => r.url === `${environment.apiUrl}/boats`
        && r.params.get('page') === '0'
        && r.params.get('size') === '5',
    );
    expect(req.request.method).toBe('GET');
    req.flush(emptyPage);
  });

  it('getBoats_should_passCorrectPageAndSizeParams_when_customValues', () => {
    service.getBoats(2, 10).subscribe();
    const req = httpMock.expectOne(
      (r) => r.params.get('page') === '2' && r.params.get('size') === '10',
    );
    expect(req.request.method).toBe('GET');
    req.flush({ ...emptyPage, number: 2, size: 10 });
  });

  // ── Page sanitisation ──────────────────────────────────────────────────────

  it('getBoats_should_sendPage0_when_pageIsNegative', () => {
    service.getBoats(-1, 5).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/boats`);
    expect(req.request.params.get('page')).toBe('0');
    req.flush(emptyPage);
  });

  it('getBoats_should_sendPage0_when_pageIsNaN', () => {
    service.getBoats(NaN, 5).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/boats`);
    expect(req.request.params.get('page')).toBe('0');
    req.flush(emptyPage);
  });

  it('getBoats_should_sendPage0_when_pageIsInfinity', () => {
    service.getBoats(Infinity, 5).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/boats`);
    expect(req.request.params.get('page')).toBe('0');
    req.flush(emptyPage);
  });

  it('getBoats_should_floorPage_when_pageIsFloat', () => {
    service.getBoats(2.9, 5).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/boats`);
    expect(req.request.params.get('page')).toBe('2');
    req.flush(emptyPage);
  });

  // ── Size sanitisation ──────────────────────────────────────────────────────

  it('getBoats_should_sendSize1_when_sizeIsZero', () => {
    service.getBoats(0, 0).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/boats`);
    expect(req.request.params.get('size')).toBe('1');
    req.flush(emptyPage);
  });

  it('getBoats_should_sendSize1_when_sizeIsNegative', () => {
    service.getBoats(0, -5).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/boats`);
    expect(req.request.params.get('size')).toBe('1');
    req.flush(emptyPage);
  });

  it('getBoats_should_sendSize1_when_sizeIsNaN', () => {
    service.getBoats(0, NaN).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/boats`);
    expect(req.request.params.get('size')).toBe('1');
    req.flush(emptyPage);
  });

  it('getBoats_should_sendSize100_when_sizeIs100', () => {
    service.getBoats(0, 100).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/boats`);
    expect(req.request.params.get('size')).toBe('100');
    req.flush(emptyPage);
  });

  it('getBoats_should_floorSize_when_sizeIsFloat', () => {
    service.getBoats(0, 5.9).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${environment.apiUrl}/boats`);
    expect(req.request.params.get('size')).toBe('5');
    req.flush(emptyPage);
  });
});





