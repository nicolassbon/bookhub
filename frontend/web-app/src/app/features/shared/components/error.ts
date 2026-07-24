import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-error-state',
  standalone: true,
  templateUrl: './error.html',
  styleUrl: './error.scss'
})
export class ErrorStateComponent {
  readonly title = input<string>('Ocurrió un error');
  readonly message = input<string>('No se pudo cargar la información requerida.');
  readonly showRetry = input<boolean>(true);
  readonly retry = output<void>();
}
