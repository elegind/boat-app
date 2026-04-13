import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Boat } from '../../shared/models/boat.model';
import { BoatRequest } from '../../shared/models/boat-request.model';
import { Page } from '../../shared/models/page.model';

/** Minimum allowed page size — mirrors backend @Min(1). */
const MIN_SIZE = 1;

/**
 * Feature service for Boat resources.
 * Communicates with GET /api/v1/boats.
 *
 * Inputs are sanitised before being sent so that NaN, negative values
 * or out-of-range sizes never reach the backend.
 */
@Injectable({ providedIn: 'root' })
export class BoatService {
  private readonly http = inject(HttpClient);

  getBoats(page: number, size: number): Observable<Page<Boat>> {
    const params = new HttpParams()
      .set('page', BoatService.sanitizePage(page))
      .set('size', BoatService.sanitizeSize(size));
    return this.http.get<Page<Boat>>(`${environment.apiUrl}/boats`, { params });
  }

  /**
   * Sends DELETE /api/v1/boats/{id}.
   * Returns an empty Observable<void> on success (204 No Content).
   */
  deleteBoat(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/boats/${id}`);
  }

  /**
   * Sends POST /api/v1/boats with the given payload.
   * Returns the created {@link Boat} on success (201 Created).
   */
  createBoat(request: BoatRequest): Observable<Boat> {
    return this.http.post<Boat>(`${environment.apiUrl}/boats`, request);
  }

  /**
   * Clamps page to a valid zero-based index.
   * NaN, Infinity, negative and non-integer values all become 0.
   */
  private static sanitizePage(page: number): number {
    if (!Number.isFinite(page) || page < 0) return 0;
    return Math.floor(page);
  }

  /**
   * Clamps size to a minimum of MIN_SIZE.
   * NaN, Infinity and non-integer values fall back to the minimum.
   */
  private static sanitizeSize(size: number): number {
    if (!Number.isFinite(size) || size < MIN_SIZE) return MIN_SIZE;
    return Math.floor(size);
  }
}




