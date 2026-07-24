import { render, screen } from '@testing-library/angular';
import { LoadingStateComponent } from './loading';

describe('LoadingStateComponent', () => {
  it('should render default loading message', async () => {
    await render(LoadingStateComponent);
    expect(screen.getByText('Cargando contenido...')).toBeTruthy();
  });

  it('should render custom loading message', async () => {
    await render(LoadingStateComponent, {
      componentInputs: { message: 'Buscando libros...' }
    });
    expect(screen.getByText('Buscando libros...')).toBeTruthy();
  });
});
