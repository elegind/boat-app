import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
} from '@angular/core';
import { NgClass } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslationService } from '../../../../core/services/translation.service';
import { BoatRequest } from '../../../../shared/models/boat-request.model';

/**
 * Reactive-form dialog for creating (and later editing) a Boat.
 *
 * The dialog owns only the form. It does NOT call the API.
 * On confirm it closes with the {@link BoatRequest} payload;
 * on cancel it closes with {@code null}.
 */
@Component({
  selector: 'app-boat-form-dialog',
  standalone: true,
  imports: [
    NgClass,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './boat-form-dialog.component.html',
  styleUrl: './boat-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoatFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<BoatFormDialogComponent>);
  protected readonly t = inject(TranslationService).t;

  protected form!: FormGroup;

  ngOnInit(): void {
    this.form = this.fb.group({
      name: [
        '',
        [
          Validators.required,
          Validators.maxLength(30),
          Validators.pattern(/^[a-zA-Z0-9-]+$/),
        ],
      ],
      description: ['', [Validators.maxLength(500)]],
    });
  }

  protected onConfirm(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.value as BoatRequest);
  }

  protected onCancel(): void {
    this.form.reset();
    this.dialogRef.close(null);
  }
}

