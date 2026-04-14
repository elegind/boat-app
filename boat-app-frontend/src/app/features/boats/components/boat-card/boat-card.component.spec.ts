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
    // First button is the visibility (view detail) button — always visible regardless of isAdmin
    buttons[0].click();
    fixture.detectChanges();

    expect(emitted.length).toBe(1);
    expect(emitted[0]).toEqual(mockBoat);
  });

  it('deleteBoat_should_emitDeleteBoat_when_trashIconClicked', () => {

    fixture.componentRef.setInput('isAdmin', true);
    fixture.detectChanges();

    const emitted: Boat[] = [];
    fixture.componentInstance.deleteBoat.subscribe((b: Boat) => emitted.push(b));

    const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('button');
    // Third button is the delete (trash) button when isAdmin is true
    buttons[2].click();
    fixture.detectChanges();

    expect(emitted.length).toBe(1);
    expect(emitted[0]).toEqual(mockBoat);
  });

  it('editBoat_should_emitEditBoat_when_penIconClicked', () => {
    // Edit button requires isAdmin = true to be rendered
    fixture.componentRef.setInput('isAdmin', true);
    fixture.detectChanges();

    const emitted: Boat[] = [];
    fixture.componentInstance.editBoat.subscribe((b: Boat) => emitted.push(b));

    const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('button');
    // Second button is the edit (pen) button when isAdmin is true
    buttons[1].click();
    fixture.detectChanges();

    expect(emitted.length).toBe(1);
    expect(emitted[0]).toEqual(mockBoat);
  });

  it('should_showEditAndDeleteIcons_when_isAdminIsTrue', () => {
    fixture.componentRef.setInput('isAdmin', true);
    fixture.detectChanges();

    const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('button');
    expect(buttons.length).toBe(3); // visibility + edit + delete
  });

  it('should_hideEditAndDeleteIcons_when_isAdminIsFalse', () => {
    fixture.componentRef.setInput('isAdmin', false);
    fixture.detectChanges();

    const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('button');
    expect(buttons.length).toBe(1); // only visibility
  });

  it('should_alwaysShowViewIcon_regardless_of_role', () => {
    fixture.componentRef.setInput('isAdmin', false);
    fixture.detectChanges();

    const firstIcon = (fixture.nativeElement as HTMLElement).querySelector('mat-icon');
    expect(firstIcon?.textContent).toContain('visibility');
  });
});
