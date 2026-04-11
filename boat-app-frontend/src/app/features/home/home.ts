import { Component, inject } from '@angular/core';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TranslationService } from '../../core/services/translation.service';

@Component({
  selector: 'app-home',
  imports: [PageHeaderComponent],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomeComponent {
  protected readonly t = inject(TranslationService).t;
}
