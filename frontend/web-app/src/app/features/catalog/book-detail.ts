import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CatalogService } from '../../core/services/catalog.service';
import { LibraryService } from '../../core/services/library.service';
import { SessionStore } from '../../core/auth/session.store';
import { BookResponse } from '../../core/api/contracts/catalog';
import { ReadingState } from '../../core/api/contracts/library';
import { LoadingStateComponent } from '../shared/components/loading';
import { ErrorStateComponent } from '../shared/components/error';
import { normalizeHttpError } from '../../core/api/error-normalizer';
import { BookReviewsComponent } from '../reviews/book-reviews';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [RouterLink, LoadingStateComponent, ErrorStateComponent, BookReviewsComponent],
  templateUrl: './book-detail.html',
  styleUrl: './book-detail.scss'
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
