import { Component, inject, input, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ReviewsService } from '../../core/services/reviews.service';
import { SessionStore } from '../../core/auth/session.store';
import { ReviewResponse } from '../../core/api/contracts/reviews';
import { LoadingStateComponent } from '../shared/components/loading';
import { EmptyStateComponent } from '../shared/components/empty';
import { normalizeHttpError } from '../../core/api/error-normalizer';

@Component({
  selector: 'app-book-reviews',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, LoadingStateComponent, EmptyStateComponent],
  templateUrl: './book-reviews.html',
  styleUrl: './book-reviews.scss'
})
export class BookReviewsComponent implements OnInit {
  readonly bookId = input.required<string>();

  private readonly fb = inject(FormBuilder);
  private readonly reviewsService = inject(ReviewsService);
  readonly sessionStore = inject(SessionStore);
  private readonly router = inject(Router);

  readonly reviews = signal<ReviewResponse[]>([]);
  readonly isLoading = signal(true);
  readonly showForm = signal(false);
  readonly isSubmitting = signal(false);
  readonly selectedRating = signal(5);
  readonly formError = signal<string | null>(null);

  readonly reviewForm = this.fb.group({
    content: ['', [Validators.required, Validators.minLength(10)]]
  });

  ngOnInit() {
    this.loadReviews();
  }

  loadReviews() {
    this.isLoading.set(true);
    this.reviewsService.getReviewsForBook(this.bookId()).subscribe({
      next: res => {
        this.reviews.set(res);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  toggleReviewForm() {
    this.showForm.update(v => !v);
  }

  setRating(rating: number) {
    this.selectedRating.set(rating);
  }

  onSubmitReview() {
    if (this.reviewForm.invalid) return;

    this.isSubmitting.set(true);
    this.formError.set(null);

    const content = this.reviewForm.get('content')?.value!;

    this.reviewsService.createReview(this.bookId(), this.selectedRating(), content).subscribe({
      next: newReview => {
        this.isSubmitting.set(false);
        this.reviews.update(prev => [newReview, ...prev]);
        this.showForm.set(false);
        this.reviewForm.reset();
      },
      error: err => {
        this.isSubmitting.set(false);
        const normalized = normalizeHttpError(err);
        this.formError.set(normalized.message);
      }
    });
  }

  navigateToLogin() {
    this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
  }
}
