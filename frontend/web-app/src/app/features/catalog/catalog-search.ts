import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { CatalogService } from '../../core/services/catalog.service';
import { BookResponse } from '../../core/api/contracts/catalog';
import { LoadingStateComponent } from '../shared/components/loading';
import { EmptyStateComponent } from '../shared/components/empty';
import { ErrorStateComponent } from '../shared/components/error';
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
  templateUrl: './catalog-search.html',
  styleUrl: './catalog-search.scss'
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
