import { Routes } from '@angular/router';
import { guestGuard, authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'catalog',
    pathMatch: 'full'
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'catalog',
    loadComponent: () => import('./features/catalog/catalog-search.component').then(m => m.CatalogSearchComponent)
  },
  {
    path: 'catalog/books/:id',
    loadComponent: () => import('./features/catalog/book-detail.component').then(m => m.BookDetailComponent)
  },
  {
    path: 'library',
    canActivate: [authGuard],
    loadComponent: () => import('./features/library/library-list.component').then(m => m.LibraryListComponent)
  }
];
