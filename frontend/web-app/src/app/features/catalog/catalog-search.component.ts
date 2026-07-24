import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { CatalogService } from '../../core/services/catalog.service';
import { BookResponse } from '../../core/api/contracts/catalog';
import { LoadingStateComponent } from '../shared/components/loading.component';
import { EmptyStateComponent } from '../shared/components/empty.component';
import { ErrorStateComponent } from '../shared/components/error.component';
import { normalizeHttpError } from '../../core/api/error-normalizer';

@Component({
  selector: 'app-catalog-search',
  standalone: true,
  imports: [
    ReactiveFormsModule, 
    RouterLink, 
    LoadingStateComponent, 
    EmptyStateComponent, 
    ErrorStateComponent
  ],
  template: `
    <div class="catalog-header">
      <h2>Explorá el Catálogo de Libros</h2>
      <p>Buscá por título, autor o palabras clave en nuestra biblioteca digital</p>

      <form [formGroup]="searchForm" class="search-form" (ngSubmit)="onSearch()">
        <div class="search-input-wrapper">
          <span class="search-icon">🔍</span>
          <input 
            type="search" 
            formControlName="query" 
            placeholder="Buscar por título o autor..." 
            aria-label="Buscar libros por título o autor" />
        </div>
      </form>
    </div>

    @if (isLoading()) {
      <app-loading-state message="Buscando libros en el catálogo..."></app-loading-state>
    } @else if (errorMessage()) {
      <app-error-state 
        title="Error al consultar el catálogo" 
        [message]="errorMessage()!" 
        (retry)="loadBooks()">
      </app-error-state>
    } @else if (books().length === 0) {
      <app-empty-state 
        title="Sin resultados" 
        description="No encontramos libros que coincidan con tu búsqueda. Probá con otros términos." 
        icon="🔍">
      </app-empty-state>
    } @else {
      <div class="books-grid">
        @for (book of books(); track book.id) {
          <a [routerLink]="['/catalog/books', book.id]" class="book-card">
            <div class="cover-wrapper">
              @if (book.coverUrl) {
                <img [src]="book.coverUrl" [alt]="'Portada de ' + book.title" loading="lazy" />
              } @else {
                <div class="cover-placeholder">
                  <span class="placeholder-icon">📖</span>
                </div>
              }
            </div>
            <div class="book-info">
              <h3 class="book-title">{{ book.title }}</h3>
              <p class="book-author">por {{ book.author }}</p>
              @if (book.publishedYear) {
                <span class="book-year">{{ book.publishedYear }}</span>
              }
            </div>
          </a>
        }
      </div>

      @if (hasMorePages()) {
        <div class="load-more-wrapper">
          <button type="button" class="btn-secondary" (click)="loadMore()">
            Cargar más libros
          </button>
        </div>
      }
    }
  `,
  styles: [`
    .catalog-header {
      margin-bottom: 32px;
      text-align: center;

      h2 {
        font-size: 2rem;
        font-weight: 700;
        margin-bottom: 8px;
      }

      p {
        color: var(--bh-color-text-secondary);
        font-size: 1.05rem;
        margin-bottom: 24px;
      }
    }

    .search-form {
      max-width: 540px;
      margin: 0 auto;
    }

    .search-input-wrapper {
      position: relative;
      display: flex;
      align-items: center;

      .search-icon {
        position: absolute;
        left: 16px;
        font-size: 1.1rem;
        color: var(--bh-color-text-muted);
        pointer-events: none;
      }

      input {
        width: 100%;
        padding: 14px 16px 14px 48px;
        border: 2px solid var(--bh-color-border);
        border-radius: var(--bh-radius-full);
        font-family: inherit;
        font-size: 1rem;
        background-color: var(--bh-color-bg-surface);
        box-shadow: var(--bh-shadow-sm);
        transition: border-color var(--bh-transition-fast), box-shadow var(--bh-transition-fast);

        &:focus {
          border-color: var(--bh-color-brand-primary);
          box-shadow: var(--bh-shadow-md);
        }
      }
    }

    .books-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 24px;
    }

    .book-card {
      display: flex;
      flex-direction: column;
      background-color: var(--bh-color-bg-surface);
      border: 1px solid var(--bh-color-border);
      border-radius: var(--bh-radius-lg);
      overflow: hidden;
      text-decoration: none;
      color: inherit;
      box-shadow: var(--bh-shadow-sm);
      transition: transform var(--bh-transition-fast), box-shadow var(--bh-transition-fast);

      &:hover {
        transform: translateY(-4px);
        box-shadow: var(--bh-shadow-md);
      }

      .cover-wrapper {
        aspect-ratio: 2/3;
        background-color: var(--bh-color-bg-elevated);
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .cover-placeholder {
          font-size: 3rem;
          color: var(--bh-color-text-muted);
        }
      }

      .book-info {
        padding: 16px;
        display: flex;
        flex-direction: column;
        gap: 4px;

        .book-title {
          font-size: 1.05rem;
          font-weight: 600;
          line-height: 1.3;
          color: var(--bh-color-text-primary);
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .book-author {
          font-size: 0.9rem;
          color: var(--bh-color-text-secondary);
        }

        .book-year {
          font-size: 0.8rem;
          color: var(--bh-color-text-muted);
          margin-top: 4px;
        }
      }
    }

    .load-more-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 36px;

      .btn-secondary {
        padding: 10px 24px;
        background-color: var(--bh-color-bg-surface);
        border: 1px solid var(--bh-color-border);
        border-radius: var(--bh-radius-md);
        font-weight: 600;
        cursor: pointer;
        transition: background-color var(--bh-transition-fast);

        &:hover {
          background-color: var(--bh-color-bg-elevated);
        }
      }
    }
  `]
})
export class CatalogSearchComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly catalogService = inject(CatalogService);

  readonly books = signal<BookResponse[]>([]);
  readonly isLoading = signal<boolean>(true);
  readonly errorMessage = signal<string | null>(null);
  readonly currentPage = signal<number>(0);
  readonly totalPages = signal<number>(0);

  readonly searchForm = this.fb.group({
    query: ['']
  });

  ngOnInit() {
    this.loadBooks();

    this.searchForm.get('query')?.valueChanges.pipe(
      debounceTime(350),
      distinctUntilChanged()
    ).subscribe(() => {
      this.currentPage.set(0);
      this.loadBooks();
    });
  }

  onSearch() {
    this.currentPage.set(0);
    this.loadBooks();
  }

  loadBooks() {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    const query = this.searchForm.get('query')?.value || '';

    this.catalogService.searchBooks(query, this.currentPage()).subscribe({
      next: res => {
        this.isLoading.set(false);
        if (this.currentPage() === 0) {
          this.books.set(res.content);
        } else {
          this.books.update(prev => [...prev, ...res.content]);
        }
        this.totalPages.set(res.totalPages);
      },
      error: err => {
        this.isLoading.set(false);
        const normalized = normalizeHttpError(err);
        this.errorMessage.set(normalized.message);
      }
    });
  }

  hasMorePages(): boolean {
    return this.currentPage() + 1 < this.totalPages();
  }

  loadMore() {
    if (this.hasMorePages()) {
      this.currentPage.update(p => p + 1);
      this.loadBooks();
    }
  }
}
