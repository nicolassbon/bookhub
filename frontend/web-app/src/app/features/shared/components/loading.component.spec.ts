import { render, screen } from '@testing-library/angular';
import { LoadingStateComponent } from './loading.component';

describe('LoadingStateComponent', () => {
  it('should render default message', async () => {
    await render(LoadingStateComponent);
    expect(screen.getByText('Cargando contenido...')).toBeTruthy();
  });

  it('should render custom message', async () => {
    await render(LoadingStateComponent, {
      componentInputs: { message: 'Buscando libros...' }
    });
    expect(screen.getByText('Buscando libros...')).toBeTruthy();
  });
});
