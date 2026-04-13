import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { BoatsComponent } from './boats.component';
import { BoatService } from '../../core/services/boat.service';
import { Page } from '../../shared/models/page.model';
import { Boat } from '../../shared/models/boat.model';

const mockBoat: Boat = {
  id: 1,
  name: 'Aurora',
  description: 'A sailing yacht',
  createdAt: '2025-01-01T00:00:00Z',
};

const mockPage: Page<Boat> = {
  content: [mockBoat],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 5,
};

describe('BoatsComponent', () => {
  let fixture: ComponentFixture<BoatsComponent>;
  let boatServiceSpy: jasmine.SpyObj<BoatService>;

  beforeEach(async () => {
    boatServiceSpy = jasmine.createSpyObj<BoatService>('BoatService', ['getBoats']);
    boatServiceSpy.getBoats.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [BoatsComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: BoatService, useValue: boatServiceSpy },
        provideNoopAnimations(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BoatsComponent);
    fixture.detectChanges();
  });

  it('loadBoats_should_showSpinner_when_isLoadingIsTrue', () => {
    fixture.componentInstance['isLoading'].set(true);
    fixture.detectChanges();
    const spinner = (fixture.nativeElement as HTMLElement).querySelector('mat-spinner, mat-progress-spinner');
    expect(spinner).toBeTruthy();
  });

  it('loadBoats_should_renderBoatCards_when_boatsAreLoaded', () => {
    const cards = (fixture.nativeElement as HTMLElement).querySelectorAll('app-boat-card');
    expect(cards.length).toBe(1);
  });

  it('openDetail_should_openDialog_when_viewDetailEmitted', () => {
    const dialogOpenSpy = spyOn(fixture.componentInstance['dialog'], 'open');
    fixture.componentInstance.openDetail(mockBoat);
    expect(dialogOpenSpy).toHaveBeenCalled();
  });
});



