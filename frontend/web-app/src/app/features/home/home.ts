import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionStore } from '../../core/auth/session.store';
import { CatalogService } from '../../core/services/catalog.service';
import { BookResponse } from '../../core/api/contracts/catalog';
import { GoalsWidgetComponent } from '../goals/goals-widget';
import { LoadingStateComponent } from '../shared/components/loading';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, GoalsWidgetComponent, LoadingStateComponent],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent implements OnInit {
  private readonly catalogService = inject(CatalogService);
  readonly sessionStore = inject(SessionStore);

  readonly featuredBooks = signal<BookResponse[]>([]);
  readonly isLoading = signal(true);

  ngOnInit() {
    this.catalogService.searchBooks('', 0, 4).subscribe({
      next: res => {
        this.featuredBooks.set(res.content);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }
}
