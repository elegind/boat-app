import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Boat } from '../../shared/models/boat.model';
import { BoatRequest } from '../../shared/models/boat-request.model';
import { BoatService } from '../../core/services/boat.service';
import { BoatCardComponent } from './components/boat-card/boat-card.component';
import { BoatDetailDialogComponent } from './components/boat-detail-dialog/boat-detail-dialog.component';
import { BoatFormDialogComponent } from './components/boat-form-dialog/boat-form-dialog.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TranslationService } from '../../core/services/translation.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-boats',
  standalone: true,
  imports: [
    BoatCardComponent,
    // BoatDetailDialogComponent and BoatFormDialogComponent are opened dynamically via MatDialog
    PageHeaderComponent,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './boats.component.html',
  styleUrl: './boats.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoatsComponent implements OnInit {
  private readonly boatService = inject(BoatService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly t = inject(TranslationService).t;
  protected readonly authService = inject(AuthService);

  protected readonly boats = signal<Boat[]>([]);
  protected readonly totalElements = signal<number>(0);
  protected readonly currentPage = signal<number>(0);
  protected readonly pageSize = signal<number>(5);
  protected readonly isLoading = signal<boolean>(false);

  readonly pageSizeOptions = [5, 10];

  ngOnInit(): void {
    this.loadBoats();
  }

  protected loadBoats(): void {
    this.isLoading.set(true);
    this.boatService.getBoats(this.currentPage(), this.pageSize()).subscribe({
      next: (page) => {
        this.boats.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load boats', err);
        this.isLoading.set(false);
      },
    });
  }

  protected onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadBoats();
  }

  openDetail(boat: Boat): void {
    this.dialog.open(BoatDetailDialogComponent, {
      data: { boat },
      width: '400px',
    });
  }

  onCreateBoat(): void {
    const dialogRef = this.dialog.open<BoatFormDialogComponent, void, BoatRequest>(
      BoatFormDialogComponent,
      { width: '480px' },
    );

    dialogRef.afterClosed().subscribe((boat: BoatRequest | undefined | null) => {
      if (!boat) return;

      this.boatService.createBoat(boat).subscribe({
        next: () => {
          this.snackBar.open(
            this.t().boats.create.success,
            undefined,
            { duration: 3000, panelClass: 'snackbar-success' },
          );
          this.currentPage.set(0);
          this.loadBoats();
        },
        error: () => {
          this.snackBar.open(
            this.t().boats.create.error,
            undefined,
            { duration: 5000, panelClass: 'snackbar-error' },
          );
        },
      });
    });
  }

  onEditBoat(boat: Boat): void {
    const dialogRef = this.dialog.open<BoatFormDialogComponent, { boat: Boat }, BoatRequest>(
      BoatFormDialogComponent,
      { data: { boat }, width: '480px' },
    );

    dialogRef.afterClosed().subscribe((request: BoatRequest | null | undefined) => {
      if (!request) return;

      const translations = this.t();
      this.boatService.updateBoat(boat.id, request).subscribe({
        next: (updated) => {
          this.snackBar.open(
            translations.boats.update.success,
            undefined,
            { duration: 3000, panelClass: 'snackbar-success' },
          );
          this.boats.update(list => list.map(b => b.id === updated.id ? updated : b));
        },
        error: () => {
          this.snackBar.open(
            translations.boats.update.error,
            undefined,
            { duration: 5000, panelClass: 'snackbar-error' },
          );
        },
      });
    });
  }

  onDeleteBoat(boat: Boat): void {    const translations = this.t();
    const title = translations.delete.confirm.title.replace('{{name}}', boat.name);
    const message = translations.delete.confirm.message.replace(/{{name}}/g, boat.name);

    const dialogData: ConfirmDialogData = {
      title,
      message,
      cancelLabel: translations.delete.confirm.cancel,
      confirmLabel: translations.delete.confirm.ok,
    };

    const dialogRef = this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, boolean>(
      ConfirmDialogComponent,
      { data: dialogData, width: '400px' },
    );

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;

      this.boatService.deleteBoat(boat.id).subscribe({
        next: () => {
          this.snackBar.open(
            translations.boats.delete.success,
            undefined,
            { duration: 3000, panelClass: 'snackbar-success' },
          );
          this._reloadAfterDeletion();
        },
        error: () => {
          this.snackBar.open(
            translations.delete.error,
            undefined,
            { duration: 5000, panelClass: 'snackbar-error' },
          );
        },
      });
    });
  }

  /**
   * Reloads the current page from the API after a deletion.
   */
  private _reloadAfterDeletion(): void {
    // boats() still holds the pre-deletion list at this point.
    // length === 1 means the page will be empty after the server confirms deletion.
    if (this.boats().length === 1 && this.currentPage() > 0) {
      this.currentPage.update((p) => p - 1);
    }
    this.loadBoats();
  }
}
