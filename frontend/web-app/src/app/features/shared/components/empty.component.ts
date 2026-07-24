import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="empty-container">
      <div class="empty-icon">{{ icon() }}</div>
      <h3 class="empty-title">{{ title() }}</h3>
      @if (description()) {
        <p class="empty-description">{{ description() }}</p>
      }
    </div>
  `,
  styles: [`
    .empty-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 40px 20px;
      background-color: var(--bh-color-bg-surface);
      border: 1px dashed var(--bh-color-border);
      border-radius: var(--bh-radius-lg);
      margin: 16px 0;
    }

    .empty-icon {
      font-size: 2.5rem;
      margin-bottom: 12px;
    }

    .empty-title {
      font-size: 1.15rem;
      font-weight: 600;
      color: var(--bh-color-text-primary);
    }

    .empty-description {
      font-size: 0.9rem;
      color: var(--bh-color-text-muted);
      margin-top: 4px;
      max-width: 400px;
    }
  `]
})
export class EmptyStateComponent {
  readonly title = input<string>('No hay elementos para mostrar');
  readonly description = input<string>('');
  readonly icon = input<string>('📭');
}
