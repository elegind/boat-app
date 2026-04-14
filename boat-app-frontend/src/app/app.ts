import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslationService } from './core/services/translation.service';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatIconModule,
    MatButtonModule,
    MatListModule,
    MatDividerModule,
    MatTooltipModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {

  protected readonly t = inject(TranslationService).t;
  protected readonly authService = inject(AuthService);
  /** true when viewport matches a handset breakpoint */
  protected readonly isMobile = signal(false);
  protected readonly sidenavOpen = signal(false);
  private readonly breakpointObserver = inject(BreakpointObserver);

  ngOnInit(): void {
    this.breakpointObserver.observe([Breakpoints.Handset]).subscribe((result) => {
      this.isMobile.set(result.matches);
      // On desktop: open sidenav by default; on mobile: closed by default
      this.sidenavOpen.set(!result.matches);
    });
  }

  protected toggleSidenav(): void {
    this.sidenavOpen.set(!this.sidenavOpen());
  }
}
