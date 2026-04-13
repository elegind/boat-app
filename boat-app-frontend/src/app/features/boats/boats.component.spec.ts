import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
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

const emptyPage: Page<Boat> = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  size: 5,
};

describe('BoatsComponent', () => {
  let fixture: ComponentFixture<BoatsComponent>;
  let boatServiceSpy: jasmine.SpyObj<BoatService>;
  let dialogSpy: jasmine.SpyObj<MatDialog>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    boatServiceSpy = jasmine.createSpyObj<BoatService>('BoatService', ['getBoats', 'deleteBoat', 'createBoat', 'updateBoat']);
    boatServiceSpy.getBoats.and.returnValue(of(mockPage));

    dialogSpy = jasmine.createSpyObj<MatDialog>('MatDialog', ['open']);
    snackBarSpy = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [BoatsComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: BoatService, useValue: boatServiceSpy },
        { provide: MatDialog, useValue: dialogSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
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
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as MatDialogRef<unknown>);
    fixture.componentInstance.openDetail(mockBoat);
    expect(dialogSpy.open).toHaveBeenCalled();
  });

  it('onDeleteBoat_should_openConfirmDialog_when_deleteBoatEmitted', () => {
    dialogSpy.open.and.returnValue({ afterClosed: () => of(false) } as MatDialogRef<unknown>);

    fixture.componentInstance.onDeleteBoat(mockBoat);

    expect(dialogSpy.open).toHaveBeenCalled();
  });

  it('onDeleteBoat_should_removeBoatFromList_when_deletionSucceeds', () => {
    // ngOnInit already consumed the first getBoats call (returned mockPage).
    // Configure the reload triggered after deletion to return an empty page.
    boatServiceSpy.getBoats.and.returnValue(of(emptyPage));
    boatServiceSpy.deleteBoat.and.returnValue(of(undefined));
    dialogSpy.open.and.returnValue({ afterClosed: () => of(true) } as MatDialogRef<unknown>);

    fixture.componentInstance.onDeleteBoat(mockBoat);
    fixture.detectChanges();

    expect(boatServiceSpy.deleteBoat).toHaveBeenCalledWith(mockBoat.id);
    // loadBoats() called twice: once on init, once after deletion
    expect(boatServiceSpy.getBoats).toHaveBeenCalledTimes(2);
    expect(fixture.componentInstance['boats']()).toEqual([]);
    expect(snackBarSpy.open).toHaveBeenCalledWith(
      jasmine.any(String),
      undefined,
      jasmine.objectContaining({ panelClass: 'snackbar-success' }),
    );
  });

  it('onDeleteBoat_should_reloadPage0_when_lastBoatOnFirstPageDeleted', () => {
    // Regression test: deleting the last boat on page 0 must reload (not show empty state)
    // even though there are still boats on the server (now shifted to page 0).
    const secondPageBoat: Boat = { id: 2, name: 'Blue Horizon', description: 'Offshore cruiser', createdAt: '2025-01-02T00:00:00Z' };
    const reloadedPage: Page<Boat> = { content: [secondPageBoat], totalElements: 1, totalPages: 1, number: 0, size: 5 };

    // ngOnInit already consumed first call; configure reload to return shifted boats
    boatServiceSpy.getBoats.and.returnValue(of(reloadedPage));
    boatServiceSpy.deleteBoat.and.returnValue(of(undefined));
    dialogSpy.open.and.returnValue({ afterClosed: () => of(true) } as MatDialogRef<unknown>);

    expect(fixture.componentInstance['currentPage']()).toBe(0);

    fixture.componentInstance.onDeleteBoat(mockBoat);
    fixture.detectChanges();

    // Must stay on page 0 and show the reloaded content
    expect(fixture.componentInstance['currentPage']()).toBe(0);
    expect(boatServiceSpy.getBoats).toHaveBeenCalledTimes(2);
    expect(fixture.componentInstance['boats']()).toEqual([secondPageBoat]);
  });

  it('onDeleteBoat_should_navigateToPreviousPage_when_lastBoatOnNonFirstPageDeleted', () => {
    const prevPageBoat: Boat = { id: 3, name: 'Calypso', description: 'Classic sloop', createdAt: '2025-01-03T00:00:00Z' };
    const prevPage: Page<Boat> = { content: [prevPageBoat], totalElements: 1, totalPages: 1, number: 0, size: 5 };

    // ngOnInit already consumed first call; configure reload (on previous page) to return prevPage
    boatServiceSpy.getBoats.and.returnValue(of(prevPage));
    boatServiceSpy.deleteBoat.and.returnValue(of(undefined));
    dialogSpy.open.and.returnValue({ afterClosed: () => of(true) } as MatDialogRef<unknown>);

    // Simulate being on page 1 with one boat (the one we'll delete)
    fixture.componentInstance['currentPage'].set(1);

    fixture.componentInstance.onDeleteBoat(mockBoat);
    fixture.detectChanges();

    // Should have navigated back to page 0 before reloading
    expect(fixture.componentInstance['currentPage']()).toBe(0);
    expect(boatServiceSpy.getBoats).toHaveBeenCalledTimes(2);
    expect(fixture.componentInstance['boats']()).toEqual([prevPageBoat]);
  });

  it('onDeleteBoat_should_showErrorSnackbar_when_deletionFails', () => {
    boatServiceSpy.deleteBoat.and.returnValue(throwError(() => new Error('Server error')));
    dialogSpy.open.and.returnValue({ afterClosed: () => of(true) } as MatDialogRef<unknown>);

    fixture.componentInstance.onDeleteBoat(mockBoat);
    fixture.detectChanges();

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      jasmine.any(String),
      undefined,
      jasmine.objectContaining({ panelClass: 'snackbar-error' }),
    );
  });

  it('onDeleteBoat_should_notDeleteBoat_when_cancelClicked', () => {
    dialogSpy.open.and.returnValue({ afterClosed: () => of(false) } as MatDialogRef<unknown>);

    fixture.componentInstance.onDeleteBoat(mockBoat);
    fixture.detectChanges();

    expect(boatServiceSpy.deleteBoat).not.toHaveBeenCalled();
  });

  it('onCreateBoat_should_openFormDialog_when_createButtonClicked', () => {
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as MatDialogRef<unknown>);

    fixture.componentInstance.onCreateBoat();

    expect(dialogSpy.open).toHaveBeenCalled();
  });

  it('onCreateBoat_should_showSuccessSnackbar_when_creationSucceeds', () => {
    const boatRequest = { name: 'New-Boat', description: 'A brand new boat' };
    const createdBoat: Boat = { id: 99, name: 'New-Boat', description: 'A brand new boat', createdAt: '2026-01-01T00:00:00Z' };

    boatServiceSpy.createBoat.and.returnValue(of(createdBoat));
    boatServiceSpy.getBoats.and.returnValue(of(mockPage));
    dialogSpy.open.and.returnValue({ afterClosed: () => of(boatRequest) } as MatDialogRef<unknown>);

    fixture.componentInstance.onCreateBoat();
    fixture.detectChanges();

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      jasmine.any(String),
      undefined,
      jasmine.objectContaining({ panelClass: 'snackbar-success' }),
    );
  });

  it('onCreateBoat_should_showErrorSnackbar_when_creationFails', () => {
    const boatRequest = { name: 'New-Boat', description: 'A brand new boat' };

    boatServiceSpy.createBoat.and.returnValue(throwError(() => new Error('Server error')));
    dialogSpy.open.and.returnValue({ afterClosed: () => of(boatRequest) } as MatDialogRef<unknown>);

    fixture.componentInstance.onCreateBoat();
    fixture.detectChanges();

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      jasmine.any(String),
      undefined,
      jasmine.objectContaining({ panelClass: 'snackbar-error' }),
    );
  });

  it('onCreateBoat_should_navigateToFirstPage_when_boatCreatedSuccessfully', () => {
    const boatRequest = { name: 'New-Boat', description: 'A brand new boat' };
    const createdBoat: Boat = { id: 99, name: 'New-Boat', description: 'A brand new boat', createdAt: '2026-01-01T00:00:00Z' };

    boatServiceSpy.createBoat.and.returnValue(of(createdBoat));
    boatServiceSpy.getBoats.and.returnValue(of(mockPage));
    dialogSpy.open.and.returnValue({ afterClosed: () => of(boatRequest) } as MatDialogRef<unknown>);

    // Simulate being on page 2 before creation
    fixture.componentInstance['currentPage'].set(2);

    fixture.componentInstance.onCreateBoat();
    fixture.detectChanges();

    // Must navigate back to page 0 after successful creation
    expect(fixture.componentInstance['currentPage']()).toBe(0);
    // getBoats called: once on ngOnInit, once after creation
    expect(boatServiceSpy.getBoats).toHaveBeenCalledTimes(2);
  });

  it('onEditBoat_should_openFormDialogWithBoat_when_editBoatEmitted', () => {
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as MatDialogRef<unknown>);

    fixture.componentInstance.onEditBoat(mockBoat);

    expect(dialogSpy.open).toHaveBeenCalledWith(
      jasmine.any(Function),
      jasmine.objectContaining({ data: { boat: mockBoat } }),
    );
  });

  it('onEditBoat_should_updateBoatInList_when_updateSucceeds', () => {
    const updatedBoat: Boat = { ...mockBoat, name: 'Updated-Aurora' };
    boatServiceSpy.updateBoat.and.returnValue(of(updatedBoat));
    dialogSpy.open.and.returnValue({
      afterClosed: () => of({ name: 'Updated-Aurora', description: mockBoat.description }),
    } as MatDialogRef<unknown>);

    fixture.componentInstance.onEditBoat(mockBoat);
    fixture.detectChanges();

    expect(boatServiceSpy.updateBoat).toHaveBeenCalledWith(mockBoat.id, jasmine.any(Object));
    const list = fixture.componentInstance['boats']();
    expect(list.find(b => b.id === updatedBoat.id)).toEqual(updatedBoat);
  });

  it('onEditBoat_should_showSuccessSnackbar_when_updateSucceeds', () => {
    const updatedBoat: Boat = { ...mockBoat, name: 'Updated-Aurora' };
    boatServiceSpy.updateBoat.and.returnValue(of(updatedBoat));
    dialogSpy.open.and.returnValue({
      afterClosed: () => of({ name: 'Updated-Aurora', description: '' }),
    } as MatDialogRef<unknown>);

    fixture.componentInstance.onEditBoat(mockBoat);
    fixture.detectChanges();

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      jasmine.any(String),
      undefined,
      jasmine.objectContaining({ panelClass: 'snackbar-success' }),
    );
  });

  it('onEditBoat_should_showErrorSnackbar_when_updateFails', () => {
    boatServiceSpy.updateBoat.and.returnValue(throwError(() => new Error('Server error')));
    dialogSpy.open.and.returnValue({
      afterClosed: () => of({ name: 'Updated-Aurora', description: '' }),
    } as MatDialogRef<unknown>);

    fixture.componentInstance.onEditBoat(mockBoat);
    fixture.detectChanges();

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      jasmine.any(String),
      undefined,
      jasmine.objectContaining({ panelClass: 'snackbar-error' }),
    );
  });
});



