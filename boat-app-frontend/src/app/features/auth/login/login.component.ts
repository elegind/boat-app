import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { map, startWith } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';
import { TranslationService } from '../../../core/services/translation.service';

/**
 * Fake login page — accepts any username + password combination.
 *
 * Navigates to {@code /home} after a short simulated delay.
 * Real OAuth / token-based integration will replace this component later.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  protected readonly t = inject(TranslationService).t;

  protected readonly form = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  /**
   * Signal derived from {@code form.statusChanges}.
   *
   * Ensures OnPush + zoneless change detection re-evaluates the template
   * whenever form validity changes (plain {@code form.invalid} is not tracked).
   */
  protected readonly isFormInvalid = toSignal(
    this.form.statusChanges.pipe(
      startWith(this.form.status),
      map(status => status !== 'VALID'),
    ),
    { initialValue: true },
  );

  protected readonly isLoading = signal(false);

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isLoading.set(true);
    setTimeout(() => {
      this.authService.login();
      this.router.navigate(['/home']);
    }, 800);
  }
}



