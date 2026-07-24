import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { normalizeHttpError } from '../../core/api/error-normalizer';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-card">
      <h2>Crear una Cuenta en BookHub</h2>
      <p class="auth-subtitle">Sumate a la comunidad de lectores</p>

      @if (errorMessage()) {
        <div class="alert error" role="alert">
          {{ errorMessage() }}
        </div>
      }

      <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="displayName">Nombre Visible</label>
          <input 
            id="displayName" 
            type="text" 
            formControlName="displayName" 
            placeholder="Nico"
            [class.invalid]="isFieldInvalid('displayName')" />
          @if (isFieldInvalid('displayName')) {
            <span class="field-error">El nombre es requerido (mínimo 2 caracteres).</span>
          }
        </div>

        <div class="form-group">
          <label for="email">Correo Electrónico</label>
          <input 
            id="email" 
            type="email" 
            formControlName="email" 
            placeholder="usuario@ejemplo.com"
            [class.invalid]="isFieldInvalid('email')" />
          @if (isFieldInvalid('email')) {
            <span class="field-error">Ingresá un correo electrónico válido.</span>
          }
        </div>

        <div class="form-group">
          <label for="password">Contraseña</label>
          <input 
            id="password" 
            type="password" 
            formControlName="password" 
            placeholder="••••••••"
            [class.invalid]="isFieldInvalid('password')" />
          @if (isFieldInvalid('password')) {
            <span class="field-error">La contraseña debe tener al menos 8 caracteres.</span>
          }
        </div>

        <button type="submit" class="btn-submit" [disabled]="isLoading() || registerForm.invalid">
          @if (isLoading()) {
            Creando cuenta...
          } @else {
            Registrarse
          }
        </button>
      </form>

      <div class="auth-footer">
        <p>¿Ya tenés una cuenta? <a routerLink="/login">Ingresá acá</a></p>
      </div>
    </div>
  `,
  styles: [`
    .auth-card {
      max-width: 440px;
      margin: 40px auto;
      padding: 32px;
      background-color: var(--bh-color-bg-surface);
      border: 1px solid var(--bh-color-border);
      border-radius: var(--bh-radius-lg);
      box-shadow: var(--bh-shadow-md);

      h2 {
        font-size: 1.5rem;
        font-weight: 700;
        margin-bottom: 6px;
      }

      .auth-subtitle {
        color: var(--bh-color-text-secondary);
        font-size: 0.95rem;
        margin-bottom: 24px;
      }
    }

    .alert.error {
      background-color: var(--bh-color-error-light);
      border: 1px solid var(--bh-color-error);
      color: var(--bh-color-error);
      padding: 10px 14px;
      border-radius: var(--bh-radius-md);
      font-size: 0.9rem;
      margin-bottom: 16px;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-bottom: 18px;

      label {
        font-weight: 600;
        font-size: 0.9rem;
      }

      input {
        padding: 10px 14px;
        border: 1px solid var(--bh-color-border);
        border-radius: var(--bh-radius-md);
        font-family: inherit;
        font-size: 0.95rem;

        &:focus {
          border-color: var(--bh-color-brand-primary);
        }

        &.invalid {
          border-color: var(--bh-color-error);
        }
      }

      .field-error {
        color: var(--bh-color-error);
        font-size: 0.8rem;
      }
    }

    .btn-submit {
      width: 100%;
      padding: 12px;
      background-color: var(--bh-color-brand-primary);
      color: #ffffff;
      border: none;
      border-radius: var(--bh-radius-md);
      font-weight: 600;
      font-size: 1rem;
      cursor: pointer;
      margin-top: 8px;
      transition: background-color var(--bh-transition-fast);

      &:hover:not(:disabled) {
        background-color: var(--bh-color-brand-hover);
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }

    .auth-footer {
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid var(--bh-color-border);
      text-align: center;
      font-size: 0.9rem;
      color: var(--bh-color-text-secondary);

      a {
        color: var(--bh-color-brand-primary);
        font-weight: 600;
        text-decoration: none;
        &:hover { text-decoration: underline; }
      }
    }
  `]
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly registerForm = this.fb.group({
    displayName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  isFieldInvalid(fieldName: string): boolean {
    const field = this.registerForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  onSubmit() {
    if (this.registerForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, password, displayName } = this.registerForm.getRawValue();

    this.authService.register(email!, password!, displayName!).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/login'], { queryParams: { registered: 'true' } });
      },
      error: err => {
        this.isLoading.set(false);
        const normalized = normalizeHttpError(err);
        if (normalized.code === 'USER_ALREADY_EXISTS') {
          this.errorMessage.set('Ya existe una cuenta registrada con este correo electrónico.');
        } else {
          this.errorMessage.set(normalized.message);
        }
      }
    });
  }
}
