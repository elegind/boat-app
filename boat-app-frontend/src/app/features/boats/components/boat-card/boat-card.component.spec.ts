import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { BoatCardComponent } from './boat-card.component';
import { Boat } from '../../../../shared/models/boat.model';

describe('BoatCardComponent', () => {
  let fixture: ComponentFixture<BoatCardComponent>;

  const mockBoat: Boat = {
    id: 1,
    name: 'Aurora',
    description: 'A graceful sailing yacht',
    createdAt: '2025-01-01T00:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BoatCardComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BoatCardComponent);
    fixture.componentRef.setInput('boat', mockBoat);
    fixture.detectChanges();
  });

  it('render_should_showBoatEmoji_when_boatIsProvided', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('🚢');
  });

  it('render_should_showBoatName_when_boatIsProvided', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Aurora');
  });

  it('viewDetail_should_emitBoat_when_visibilityIconClicked', () => {
    const emitted: Boat[] = [];
    fixture.componentInstance.viewDetail.subscribe((b: Boat) => emitted.push(b));

    const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('button');
    // First button is the visibility (view detail) button
    buttons[0].click();
    fixture.detectChanges();

    expect(emitted.length).toBe(1);
    expect(emitted[0]).toEqual(mockBoat);
  });
});

