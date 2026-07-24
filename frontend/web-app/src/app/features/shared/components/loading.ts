import { Component, input } from '@angular/core';

@Component({
  selector: 'app-loading-state',
  standalone: true,
  templateUrl: './loading.html',
  styleUrl: './loading.scss'
})
export class LoadingStateComponent {
  readonly message = input<string>('Cargando contenido...');
}
