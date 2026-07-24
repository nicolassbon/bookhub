import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LibraryService } from '../../core/services/library.service';
import { UserBookResponse, ReadingState } from '../../core/api/contracts/library';
import { LoadingStateComponent } from '../shared/components/loading';
import { EmptyStateComponent } from '../shared/components/empty';
import { ErrorStateComponent } from '../shared/components/error';
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
  templateUrl: './library-list.html',
  styleUrl: './library-list.scss'
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
