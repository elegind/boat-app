import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
} from '@angular/core';
import { NgClass } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { map, startWith } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslationService } from '../../../../core/services/translation.service';
import { Boat } from '../../../../shared/models/boat.model';
import { BoatRequest } from '../../../../shared/models/boat-request.model';

/**
 * Reactive-form dialog for creating and editing a Boat.
 *
 * The dialog owns only the form. It does NOT call the API.
 * On confirm it closes with the {@link BoatRequest} payload;
 * on cancel it closes with {@code null}.
 *
 * When opened from {@link MatDialog} with {@code data: { boat }} it switches
 * to edit mode: the title and confirm label change, and the form is pre-filled.
 *
 * Uses {@code toSignal} to convert form validity into a signal so that
 * zoneless + OnPush change detection re-evaluates the template correctly
 * whenever the form transitions between valid and invalid states.
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
  private readonly fb             = inject(FormBuilder);
  private readonly dialogRef      = inject(MatDialogRef<BoatFormDialogComponent>);
  private readonly dialogData     = inject<{ boat: Boat | null }>(MAT_DIALOG_DATA, { optional: true });
  protected readonly t            = inject(TranslationService).t;

  /**
   * Form built as a class field (not in ngOnInit) so that {@code toSignal}
   * can reference it during field initialisation, before the lifecycle hooks run.
   */
  protected readonly form = this.fb.group({
    name: [
      '',
      [
        Validators.required,
        Validators.maxLength(30),
          Validators.pattern(/^[a-zA-Z0-9 -]+$/),
      ],
    ],
    description: ['', [Validators.maxLength(500)]],
  });

  /**
   * Signal derived from {@code form.statusChanges}.
   *
   * With zoneless change detection, plain property reads like {@code form.invalid}
   * are not tracked by the signal graph and therefore do not trigger re-renders
   * on OnPush components. Converting the status observable to a signal ensures
   * Angular re-evaluates the template whenever validity changes.
   */
  protected readonly isFormInvalid = toSignal(
    this.form.statusChanges.pipe(
      startWith(this.form.status),
      map(status => status !== 'VALID'),
    ),
    { initialValue: true },
  );

  protected readonly dialogTitle = computed(() =>
    this.dialogData?.boat
      ? this.t().boats.form.edit.title
      : this.t().boats.create.dialog.title,
  );

  protected readonly confirmLabel = computed(() =>
    this.dialogData?.boat
      ? this.t().boats.form.save
      : this.t().boats.form.create,
  );

  /** Patch form with existing values when opening in edit mode. */
  ngOnInit(): void {
    const boatToEdit = this.dialogData?.boat ?? null;
    if (boatToEdit) {
      this.form.patchValue({
        name: boatToEdit.name,
        description: boatToEdit.description ?? '',
      });
    }
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
