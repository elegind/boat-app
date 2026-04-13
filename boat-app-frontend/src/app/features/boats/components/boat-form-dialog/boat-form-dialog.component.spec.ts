import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BoatFormDialogComponent } from './boat-form-dialog.component';
import { Boat } from '../../../../shared/models/boat.model';

describe('BoatFormDialogComponent', () => {
  let fixture: ComponentFixture<BoatFormDialogComponent>;
  let component: BoatFormDialogComponent;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<BoatFormDialogComponent>>;

  beforeEach(async () => {
    dialogRefSpy = jasmine.createSpyObj<MatDialogRef<BoatFormDialogComponent>>(
      'MatDialogRef',
      ['close'],
    );

    await TestBed.configureTestingModule({
      imports: [BoatFormDialogComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideNoopAnimations(),
        { provide: MatDialogRef, useValue: dialogRefSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BoatFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('form_should_disableCreateButton_when_formIsInvalid', () => {
    // Form is invalid on init because name is required but empty
    expect(component['form'].invalid).toBeTrue();
    const button = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>(
      'button[mat-raised-button]',
    );
    expect(button?.disabled).toBeTrue();
  });

  it('form_should_showRequiredError_when_nameIsEmpty', () => {
    const nameControl = component['form'].get('name');
    nameControl?.markAsTouched();
    fixture.detectChanges();
    const errors = (fixture.nativeElement as HTMLElement).querySelectorAll('mat-error');
    expect(errors.length).toBeGreaterThan(0);
  });

  it('form_should_showPatternError_when_nameContainsSpecialChars', () => {
    const nameControl = component['form'].get('name');
    nameControl?.setValue('My Boat!');
    nameControl?.markAsTouched();
    fixture.detectChanges();
    const errors = (fixture.nativeElement as HTMLElement).querySelectorAll('mat-error');
    expect(errors.length).toBeGreaterThan(0);
  });

  it('form_should_showMaxLengthError_when_nameExceeds30Chars', () => {
    const nameControl = component['form'].get('name');
    nameControl?.setValue('a'.repeat(31));
    nameControl?.markAsTouched();
    fixture.detectChanges();
    const errors = (fixture.nativeElement as HTMLElement).querySelectorAll('mat-error');
    expect(errors.length).toBeGreaterThan(0);
  });

  it('form_should_showMaxLengthError_when_descriptionExceeds500Chars', () => {
    component['form'].get('name')?.setValue('Valid-Name');
    const descControl = component['form'].get('description');
    descControl?.setValue('a'.repeat(501));
    descControl?.markAsTouched();
    fixture.detectChanges();
    const errors = (fixture.nativeElement as HTMLElement).querySelectorAll('mat-error');
    expect(errors.length).toBeGreaterThan(0);
  });

  it('onConfirm_should_closeDialogWithFormValue_when_formIsValid', () => {
    component['form'].setValue({ name: 'Valid-Boat', description: 'A valid description' });
    component['onConfirm']();
    expect(dialogRefSpy.close).toHaveBeenCalledWith({
      name: 'Valid-Boat',
      description: 'A valid description',
    });
  });

  it('onCancel_should_closeDialogWithNull_when_cancelClicked', () => {
    component['onCancel']();
    expect(dialogRefSpy.close).toHaveBeenCalledWith(null);
  });

  it('form_should_showCreateTitle_when_noBoatProvided', () => {
    const titleEl = (fixture.nativeElement as HTMLElement).querySelector('[mat-dialog-title]');
    expect(titleEl?.textContent?.trim()).toContain('Create a new boat');
  });
});

describe('BoatFormDialogComponent (edit mode)', () => {
  let fixture: ComponentFixture<BoatFormDialogComponent>;
  let component: BoatFormDialogComponent;

  const mockBoat: Boat = {
    id: 1,
    name: 'Aurora',
    description: 'A graceful sailing yacht',
    createdAt: '2025-01-01T00:00:00Z',
  };

  beforeEach(async () => {
    const dialogRefSpy = jasmine.createSpyObj<MatDialogRef<BoatFormDialogComponent>>(
      'MatDialogRef',
      ['close'],
    );

    await TestBed.configureTestingModule({
      imports: [BoatFormDialogComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideNoopAnimations(),
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: { boat: mockBoat } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BoatFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('form_should_prefillForm_when_openedInEditMode', () => {
    expect(component['form'].get('name')?.value).toBe('Aurora');
    expect(component['form'].get('description')?.value).toBe('A graceful sailing yacht');
  });

  it('form_should_showEditTitle_when_boatProvided', () => {
    const titleEl = (fixture.nativeElement as HTMLElement).querySelector('[mat-dialog-title]');
    expect(titleEl?.textContent?.trim()).toContain('Edit boat');
  });
});


