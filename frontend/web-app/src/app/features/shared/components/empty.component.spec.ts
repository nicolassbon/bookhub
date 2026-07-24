import { render, screen } from '@testing-library/angular';
import { EmptyStateComponent } from './empty.component';

describe('EmptyStateComponent', () => {
  it('should render default title and icon', async () => {
    await render(EmptyStateComponent);
    expect(screen.getByText('No hay elementos para mostrar')).toBeTruthy();
    expect(screen.getByText('📭')).toBeTruthy();
  });

  it('should render custom title and description', async () => {
    await render(EmptyStateComponent, {
      componentInputs: { title: 'Sin resultados', description: 'Intenta con otro término' }
    });
    expect(screen.getByText('Sin resultados')).toBeTruthy();
    expect(screen.getByText('Intenta con otro término')).toBeTruthy();
  });
});
