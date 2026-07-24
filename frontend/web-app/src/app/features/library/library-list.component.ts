import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LibraryService } from '../../core/services/library.service';
import { UserBookResponse, ReadingState } from '../../core/api/contracts/library';
import { LoadingStateComponent } from '../shared/components/loading.component';
import { EmptyStateComponent } from '../shared/components/empty.component';
import { ErrorStateComponent } from '../shared/components/error.component';
import { normalizeHttpError } from '../../core/api/error-normalizer';

@Component({
  selector: 'app-library-list',
  standalone: true,
  imports: [
    ReactiveFormsModule, 
    RouterLink, 
    LoadingStateComponent, 
    EmptyStateComponent, 
    ErrorStateComponent
  ],
  template: `
    <div class="library-header">
      <h2>Mi Biblioteca de Lectura</h2>
      <p>Gestioná tus libros guardados y actualizá tu avance página por página</p>

      <div class="filter-tabs" role="tablist">
        <button 
          type="button" 
          role="tab" 
          [attr.aria-selected]="activeFilter() === null"
          [class.active]="activeFilter() === null" 
          (click)="setFilter(null)">
          Todos
        </button>
        <button 
          type="button" 
          role="tab" 
          [attr.aria-selected]="activeFilter() === 'READING'"
          [class.active]="activeFilter() === 'READING'" 
          (click)="setFilter('READING')">
          📖 Leyendo
        </button>
        <button 
          type="button" 
          role="tab" 
          [attr.aria-selected]="activeFilter() === 'WANT_TO_READ'"
          [class.active]="activeFilter() === 'WANT_TO_READ'" 
          (click)="setFilter('WANT_TO_READ')">
          📚 Quiero Leer
        </button>
        <button 
          type="button" 
          role="tab" 
          [attr.aria-selected]="activeFilter() === 'READ'"
          [class.active]="activeFilter() === 'READ'" 
          (click)="setFilter('READ')">
          ✅ Leídos
        </button>
      </div>
    </div>

    @if (isLoading()) {
      <app-loading-state message="Cargando tu biblioteca..."></app-loading-state>
    } @else if (errorMessage()) {
      <app-error-state 
        title="Error al cargar la biblioteca" 
        [message]="errorMessage()!" 
        (retry)="loadLibrary()">
      </app-error-state>
    } @else if (userBooks().length === 0) {
      <app-empty-state 
        title="Tu biblioteca está vacía" 
        description="Todavía no agregaste libros a esta sección. Explorá el catálogo para sumar nuevas lecturas." 
        icon="📚">
      </app-empty-state>
    } @else {
      <div class="library-grid">
        @for (item of userBooks(); track item.id) {
          <div class="library-item-card">
            <div class="item-cover">
              @if (item.book.coverUrl) {
                <img [src]="item.book.coverUrl" [alt]="'Portada de ' + item.book.title" />
              } @else {
                <div class="placeholder-cover">📖</div>
              }
            </div>

            <div class="item-content">
              <div class="item-header">
                <a [routerLink]="['/catalog/books', item.book.id]" class="item-title">
                  {{ item.book.title }}
                </a>
                <span class="item-author">por {{ item.book.author }}</span>
              </div>

              <div class="progress-section">
                <div class="progress-labels">
                  <span>Progreso de lectura</span>
                  <span class="percentage">{{ item.percentageCompleted }}%</span>
                </div>
                <div class="progress-bar">
                  <div class="progress-fill" [style.width.%]="item.percentageCompleted"></div>
                </div>
                <span class="pages-text">Página {{ item.currentPage }} de {{ item.totalPages || item.book.pageCount }}</span>
              </div>

              <div class="update-progress-row">
                <input 
                  #pageInput 
                  type="number" 
                  min="0" 
                  [max]="item.totalPages || item.book.pageCount" 
                  [value]="item.currentPage" 
                  placeholder="Pág" />
                <button 
                  type="button" 
                  class="btn-update" 
                  (click)="updateProgress(item.id, pageInput.value)">
                  Actualizar
                </button>
              </div>
            </div>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .library-header {
      margin-bottom: 32px;

      h2 {
        font-size: 2rem;
        font-weight: 700;
        margin-bottom: 6px;
      }

      p {
        color: var(--bh-color-text-secondary);
        font-size: 1rem;
        margin-bottom: 20px;
      }
    }

    .filter-tabs {
      display: flex;
      gap: 10px;
      border-bottom: 1px solid var(--bh-color-border);
      padding-bottom: 12px;

      button {
        background: none;
        border: none;
        padding: 8px 16px;
        border-radius: var(--bh-radius-full);
        font-weight: 600;
        font-size: 0.9rem;
        color: var(--bh-color-text-secondary);
        cursor: pointer;
        transition: all var(--bh-transition-fast);

        &:hover {
          background-color: var(--bh-color-bg-elevated);
        }

        &.active {
          background-color: var(--bh-color-brand-primary);
          color: #ffffff;
        }
      }
    }

    .library-grid {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .library-item-card {
      display: flex;
      gap: 24px;
      background-color: var(--bh-color-bg-surface);
      border: 1px solid var(--bh-color-border);
      border-radius: var(--bh-radius-lg);
      padding: 20px;
      box-shadow: var(--bh-shadow-sm);

      @media (max-width: 600px) {
        flex-direction: column;
      }
    }

    .item-cover {
      width: 100px;
      height: 145px;
      background-color: var(--bh-color-bg-elevated);
      border-radius: var(--bh-radius-md);
      overflow: hidden;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .placeholder-cover {
        font-size: 2.5rem;
      }
    }

    .item-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }

    .item-header {
      margin-bottom: 12px;

      .item-title {
        font-size: 1.2rem;
        font-weight: 700;
        color: var(--bh-color-text-primary);
        text-decoration: none;
        display: block;

        &:hover {
          color: var(--bh-color-brand-primary);
        }
      }

      .item-author {
        font-size: 0.9rem;
        color: var(--bh-color-text-secondary);
      }
    }

    .progress-section {
      margin-bottom: 14px;

      .progress-labels {
        display: flex;
        justify-content: space-between;
        font-size: 0.85rem;
        font-weight: 600;
        margin-bottom: 4px;
      }

      .progress-bar {
        height: 8px;
        background-color: var(--bh-color-border);
        border-radius: var(--bh-radius-full);
        overflow: hidden;
        margin-bottom: 4px;

        .progress-fill {
          height: 100%;
          background-color: var(--bh-color-success);
          transition: width var(--bh-transition-normal);
        }
      }

      .pages-text {
        font-size: 0.8rem;
        color: var(--bh-color-text-muted);
      }
    }

    .update-progress-row {
      display: flex;
      align-items: center;
      gap: 8px;

      input {
        width: 90px;
        padding: 6px 10px;
        border: 1px solid var(--bh-color-border);
        border-radius: var(--bh-radius-md);
        font-family: inherit;
        font-size: 0.9rem;
      }

      .btn-update {
        padding: 6px 14px;
        background-color: var(--bh-color-brand-primary);
        color: #ffffff;
        border: none;
        border-radius: var(--bh-radius-md);
        font-weight: 600;
        font-size: 0.85rem;
        cursor: pointer;

        &:hover {
          background-color: var(--bh-color-brand-hover);
        }
      }
    }
  `]
})
export class LibraryListComponent implements OnInit {
  private readonly libraryService = inject(LibraryService);

  readonly userBooks = signal<UserBookResponse[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly activeFilter = signal<ReadingState | null>(null);

  ngOnInit() {
    this.loadLibrary();
  }

  setFilter(filter: ReadingState | null) {
    this.activeFilter.set(filter);
    this.loadLibrary();
  }

  loadLibrary() {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    const filter = this.activeFilter() || undefined;

    this.libraryService.getUserBooks(filter).subscribe({
      next: books => {
        this.userBooks.set(books);
        this.isLoading.set(false);
      },
      error: err => {
        this.isLoading.set(false);
        const normalized = normalizeHttpError(err);
        this.errorMessage.set(normalized.message);
      }
    });
  }

  updateProgress(entryId: string, pageVal: string) {
    const page = parseInt(pageVal, 10);
    if (isNaN(page) || page < 0) return;

    this.libraryService.updateReadingProgress(entryId, page).subscribe({
      next: updated => {
        this.userBooks.update(list => 
          list.map(item => item.id === entryId ? updated : item)
        );
      },
      error: err => {
        const normalized = normalizeHttpError(err);
        alert(normalized.message);
      }
    });
  }
}
