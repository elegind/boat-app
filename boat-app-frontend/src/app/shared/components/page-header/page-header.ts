import { Component, input } from '@angular/core';

/**
 * Reusable responsive page header.
 *
 * Usage:
 *   <app-page-header title="My Page" subtitle="Optional subtitle" />
 */
@Component({
  selector: 'app-page-header',
  templateUrl: './page-header.html',
  styleUrl: './page-header.scss',
})
export class PageHeaderComponent {
  /** Main heading — required */
  readonly title = input.required<string>();

  /** Optional sub-heading */
  readonly subtitle = input<string>('');
}
