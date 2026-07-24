import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CatalogService } from '../../core/services/catalog.service';
import { LibraryService } from '../../core/services/library.service';
import { SessionStore } from '../../core/auth/session.store';
import { BookResponse } from '../../core/api/contracts/catalog';
import { ReadingState } from '../../core/api/contracts/library';
import { LoadingStateComponent } from '../shared/components/loading.component';
import { ErrorStateComponent } from '../shared/components/error.component';
import { normalizeHttpError } from '../../core/api/error-normalizer';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [RouterLink, LoadingStateComponent, ErrorStateComponent],
  template: `
    @if (isLoading()) {
      <app-loading-state message="Cargando detalles del libro..."></app-loading-state>
    } @else if (errorMessage()) {
      <app-error-state 
        title="Libro no encontrado" 
        [message]="errorMessage()!" 
        (retry)="loadBookDetail()">
      </app-error-state>
    } @else if (book()) {
      <div class="book-detail-container">
        <a routerLink="/catalog" class="back-link">← Volver al catálogo</a>

        <div class="book-detail-grid">
          <div class="cover-section">
            <div class="cover-card">
              @if (book()!.coverUrl) {
                <img [src]="book()!.coverUrl" [alt]="'Portada de ' + book()!.title" />
              } @else {
                <div class="cover-placeholder">📖</div>
              }
            </div>

            <div class="actions-wrapper">
              @if (isSuccessMessage()) {
                <div class="alert success" role="status">
                  {{ isSuccessMessage() }}
                </div>
              }

              <div class="shelf-buttons">
                <button 
                  type="button" 
                  class="btn-action want"
                  [disabled]="isAdding()"
                  (click)="addBook('WANT_TO_READ')">
                  📚 Quiero Leer
                </button>
                <button 
                  type="button" 
                  class="btn-action reading"
                  [disabled]="isAdding()"
                  (click)="addBook('READING')">
                  📖 Leyendo
                </button>
                <button 
                  type="button" 
                  class="btn-action read"
                  [disabled]="isAdding()"
                  (click)="addBook('READ')">
                  ✅ Leído
                </button>
              </div>
            </div>
          </div>

          <div class="info-section">
            <h1 class="book-title">{{ book()!.title }}</h1>
            <h2 class="book-author">por {{ book()!.author }}</h2>

            <div class="meta-row">
              @if (book()!.publishedYear) {
                <span class="meta-badge">📅 {{ book()!.publishedYear }}</span>
              }
              @if (book()!.pageCount) {
                <span class="meta-badge">📄 {{ book()!.pageCount }} páginas</span>
              }
              @if (book()!.isbn) {
                <span class="meta-badge">🏷️ ISBN: {{ book()!.isbn }}</span>
              }
            </div>

            <div class="description-box">
              <h3>Sinopsis</h3>
              <p>{{ book()!.description || 'Sin descripción disponible.' }}</p>
            </div>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .book-detail-container {
      max-width: 960px;
      margin: 0 auto;
    }

    .back-link {
      display: inline-block;
      margin-bottom: 20px;
      color: var(--bh-color-text-secondary);
      text-decoration: none;
      font-weight: 500;
      transition: color var(--bh-transition-fast);

      &:hover {
        color: var(--bh-color-brand-primary);
      }
    }

    .book-detail-grid {
      display: grid;
      grid-template-columns: 280px 1fr;
      gap: 40px;

      @media (max-width: 768px) {
        grid-template-columns: 1fr;
      }
    }

    .cover-card {
      aspect-ratio: 2/3;
      background-color: var(--bh-color-bg-surface);
      border: 1px solid var(--bh-color-border);
      border-radius: var(--bh-radius-lg);
      overflow: hidden;
      box-shadow: var(--bh-shadow-md);
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .cover-placeholder {
        font-size: 5rem;
      }
    }

    .actions-wrapper {
      margin-top: 20px;

      .alert.success {
        background-color: var(--bh-color-success-light);
        color: var(--bh-color-success);
        border: 1px solid rgba(59, 128, 82, 0.2);
        padding: 10px;
        border-radius: var(--bh-radius-md);
        font-size: 0.9rem;
        margin-bottom: 12px;
        text-align: center;
      }

      .shelf-buttons {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .btn-action {
          width: 100%;
          padding: 10px;
          border: 1px solid var(--bh-color-border);
          border-radius: var(--bh-radius-md);
          background-color: var(--bh-color-bg-surface);
          font-weight: 600;
          cursor: pointer;
          transition: all var(--bh-transition-fast);

          &:hover:not(:disabled) {
            background-color: var(--bh-color-brand-primary);
            color: #ffffff;
            border-color: var(--bh-color-brand-primary);
          }

          &:disabled {
            opacity: 0.6;
            cursor: not-allowed;
          }
        }
      }
    }

    .info-section {
      .book-title {
        font-size: 2.2rem;
        font-weight: 700;
        line-height: 1.2;
        margin-bottom: 6px;
      }

      .book-author {
        font-size: 1.2rem;
        font-weight: 500;
        color: var(--bh-color-text-secondary);
        margin-bottom: 16px;
      }

      .meta-row {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-bottom: 24px;

        .meta-badge {
          background-color: var(--bh-color-bg-surface);
          border: 1px solid var(--bh-color-border);
          padding: 4px 12px;
          border-radius: var(--bh-radius-full);
          font-size: 0.85rem;
          color: var(--bh-color-text-secondary);
        }
      }

      .description-box {
        background-color: var(--bh-color-bg-surface);
        border: 1px solid var(--bh-color-border);
        border-radius: var(--bh-radius-lg);
        padding: 24px;

        h3 {
          font-size: 1.1rem;
          font-weight: 600;
          margin-bottom: 10px;
        }

        p {
          line-height: 1.6;
          color: var(--bh-color-text-secondary);
        }
      }
    }
  `]
})
export class BookDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly catalogService = inject(CatalogService);
  private readonly libraryService = inject(LibraryService);
  private readonly sessionStore = inject(SessionStore);

  readonly book = signal<BookResponse | null>(null);
  readonly isLoading = signal(true);
  readonly isAdding = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly isSuccessMessage = signal<string | null>(null);

  ngOnInit() {
    this.loadBookDetail();
  }

  loadBookDetail() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.errorMessage.set('Identificador de libro no válido.');
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.catalogService.getBookById(id).subscribe({
      next: b => {
        this.book.set(b);
        this.isLoading.set(false);
      },
      error: err => {
        this.isLoading.set(false);
        const normalized = normalizeHttpError(err);
        this.errorMessage.set(normalized.message);
      }
    });
  }

  addBook(state: ReadingState) {
    if (!this.sessionStore.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }

    const currentBook = this.book();
    if (!currentBook) return;

    this.isAdding.set(true);
    this.isSuccessMessage.set(null);

    this.libraryService.addBookToLibrary(currentBook.id, state).subscribe({
      next: () => {
        this.isAdding.set(false);
        const stateName = state === 'READING' ? 'Leyendo' : state === 'READ' ? 'Leído' : 'Quiero Leer';
        this.isSuccessMessage.set(`Agregado a tu estantería "${stateName}" correctamente.`);
      },
      error: err => {
        this.isAdding.set(false);
        const normalized = normalizeHttpError(err);
        alert(normalized.message);
      }
    });
  }
}
