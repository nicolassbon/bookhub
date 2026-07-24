import { render, screen } from '@testing-library/angular';
import { EmptyStateComponent } from './empty';

describe('EmptyStateComponent', () => {
  it('should render default title and icon', async () => {
    await render(EmptyStateComponent);
    expect(screen.getByText('No hay elementos para mostrar')).toBeTruthy();
    expect(screen.getByText('📭')).toBeTruthy();
  });

  it('should render custom title and description', async () => {
    await render(EmptyStateComponent, {
      componentInputs: {
        title: 'Biblioteca vacía',
        description: 'No agregaste libros aún.',
        icon: '📚'
      }
    });
    expect(screen.getByText('Biblioteca vacía')).toBeTruthy();
    expect(screen.getByText('No agregaste libros aún.')).toBeTruthy();
    expect(screen.getByText('📚')).toBeTruthy();
  });
});
