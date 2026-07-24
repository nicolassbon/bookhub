import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  templateUrl: './empty.html',
  styleUrl: './empty.scss'
})
export class EmptyStateComponent {
  readonly title = input<string>('No hay elementos para mostrar');
  readonly description = input<string>('');
  readonly icon = input<string>('📭');
}
