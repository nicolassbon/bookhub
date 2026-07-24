import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ProfileService } from '../../core/services/profile.service';
import { SessionStore } from '../../core/auth/session.store';
import { UserResponse } from '../../core/api/contracts/auth';
import { LoadingStateComponent } from '../shared/components/loading';
import { normalizeHttpError } from '../../core/api/error-normalizer';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, LoadingStateComponent],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class ProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly sessionStore = inject(SessionStore);

  readonly user = signal<UserResponse | null>(null);
  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly successMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly profileForm = this.fb.group({
    displayName: ['', [Validators.required, Validators.minLength(2)]]
  });

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.profileService.getProfile().subscribe({
      next: u => {
        this.user.set(u);
        this.profileForm.patchValue({ displayName: u.displayName });
        this.isLoading.set(false);
      },
      error: err => {
        this.isLoading.set(false);
        const normalized = normalizeHttpError(err);
        this.errorMessage.set(normalized.message);
      }
    });
  }

  onSubmit() {
    if (this.profileForm.invalid) return;

    this.isSaving.set(true);
    this.successMessage.set(null);
    this.errorMessage.set(null);

    const displayName = this.profileForm.get('displayName')?.value!;

    this.profileService.updateProfile(displayName).subscribe({
      next: updated => {
        this.user.set(updated);
        this.sessionStore.setUser(updated);
        this.isSaving.set(false);
        this.successMessage.set('Perfil actualizado con éxito.');
      },
      error: err => {
        this.isSaving.set(false);
        const normalized = normalizeHttpError(err);
        this.errorMessage.set(normalized.message);
      }
    });
  }
}
