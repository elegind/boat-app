import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { DatePipe } from '@angular/common';
import { Boat } from '../../../../shared/models/boat.model';
import { TranslationService } from '../../../../core/services/translation.service';

@Component({
  selector: 'app-boat-detail-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, DatePipe],
  templateUrl: './boat-detail-dialog.component.html',
  styleUrl: './boat-detail-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoatDetailDialogComponent {
  protected readonly data = inject<{ boat: Boat }>(MAT_DIALOG_DATA);
  protected readonly dialogRef = inject(MatDialogRef<BoatDetailDialogComponent>);
  protected readonly t = inject(TranslationService).t;
}

