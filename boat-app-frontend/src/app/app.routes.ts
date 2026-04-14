import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  {
    path: 'home',
    loadComponent: () =>
      import('./features/boats/boats.component').then((m) => m.BoatsComponent),
    canActivate: [authGuard],
  },
  { path: '**', redirectTo: 'home' },
];
