import { render, screen, fireEvent } from '@testing-library/angular';
import { ErrorStateComponent } from './error.component';

describe('ErrorStateComponent', () => {
  it('should render title and message', async () => {
    await render(ErrorStateComponent, {
      componentInputs: { title: 'Error 500', message: 'Fallo el servidor' }
    });
    expect(screen.getByText('Error 500')).toBeTruthy();
    expect(screen.getByText('Fallo el servidor')).toBeTruthy();
  });

  it('should emit retry event on button click', async () => {
    let retried = false;
    await render(ErrorStateComponent, {
      on: {
        retry: () => { retried = true; }
      }
    });

    const button = screen.getByRole('button', { name: /reintentar/i });
    await fireEvent.click(button);
    expect(retried).toBe(true);
  });
});
