import { Component } from '@angular/core';
import { ShellComponent } from './core/components/shell';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ShellComponent],
  template: `<app-shell></app-shell>`
})
export class App {}
