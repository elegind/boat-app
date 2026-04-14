import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Boat } from '../../../../shared/models/boat.model';
import { TranslationService } from '../../../../core/services/translation.service';

@Component({
  selector: 'app-boat-card',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './boat-card.component.html',
  styleUrl: './boat-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoatCardComponent {
  readonly boat = input.required<Boat>();

  readonly isAdmin = input<boolean>(false);
  readonly viewDetail = output<Boat>();
  readonly editBoat = output<Boat>();
  readonly deleteBoat = output<Boat>();

  protected readonly t = inject(TranslationService).t;
}

