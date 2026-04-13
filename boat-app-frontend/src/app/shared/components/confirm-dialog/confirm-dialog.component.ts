import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

/** Data injected into the dialog by the caller. */
export interface ConfirmDialogData {
  title: string;
  message: string;
  cancelLabel: string;
  confirmLabel: string;
}

/**
 * Generic confirmation dialog for any action requiring user acknowledgement.
 *
 * The dialog is intentionally decoupled from any feature:
 * the caller supplies title, message and button labels via {@link MAT_DIALOG_DATA}.
 * It closes with {@code true} on confirm and {@code false} on cancel.
 */
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './confirm-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmDialogComponent {
  protected readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
  protected readonly dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }
}
