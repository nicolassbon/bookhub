import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-error-state',
  standalone: true,
  template: `
    <div class="error-container" role="alert">
      <div class="error-icon">⚠️</div>
      <h3 class="error-title">{{ title() }}</h3>
      <p class="error-message">{{ message() }}</p>
      @if (showRetry()) {
        <button type="button" class="btn-retry" (click)="retry.emit()">Reintentar</button>
      }
    </div>
  `,
  styles: [`
    .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 32px 20px;
      background-color: var(--bh-color-error-light);
      border: 1px solid rgba(201, 74, 74, 0.2);
      border-radius: var(--bh-radius-lg);
      margin: 16px 0;
    }

    .error-icon {
      font-size: 2.2rem;
      margin-bottom: 8px;
    }

    .error-title {
      font-size: 1.1rem;
      font-weight: 600;
      color: var(--bh-color-error);
    }

    .error-message {
      font-size: 0.9rem;
      color: var(--bh-color-text-secondary);
      margin-top: 4px;
      margin-bottom: 12px;
    }

    .btn-retry {
      background-color: var(--bh-color-error);
      color: #ffffff;
      border: none;
      padding: 8px 18px;
      border-radius: var(--bh-radius-md);
      font-weight: 500;
      cursor: pointer;
      transition: opacity var(--bh-transition-fast);

      &:hover {
        opacity: 0.9;
      }
    }
  `]
})
export class ErrorStateComponent {
  readonly title = input<string>('Ocurrió un error');
  readonly message = input<string>('No se pudo cargar la información requerida.');
  readonly showRetry = input<boolean>(true);
  readonly retry = output<void>();
}
