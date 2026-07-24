import { Routes } from '@angular/router';
import { guestGuard, authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register').then(m => m.RegisterComponent)
  },
  {
    path: 'catalog',
    loadComponent: () => import('./features/catalog/catalog-search').then(m => m.CatalogSearchComponent)
  },
  {
    path: 'catalog/books/:id',
    loadComponent: () => import('./features/catalog/book-detail').then(m => m.BookDetailComponent)
  },
  {
    path: 'library',
    canActivate: [authGuard],
    loadComponent: () => import('./features/library/library-list').then(m => m.LibraryListComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile').then(m => m.ProfileComponent)
  },
  {
    path: 'notifications',
    canActivate: [authGuard],
    loadComponent: () => import('./features/notifications/notifications').then(m => m.NotificationsComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
