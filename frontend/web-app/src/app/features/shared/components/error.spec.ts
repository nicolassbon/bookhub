import { render, screen, fireEvent } from '@testing-library/angular';
import { ErrorStateComponent } from './error';

describe('ErrorStateComponent', () => {
  it('should render title and message', async () => {
    await render(ErrorStateComponent, {
      componentInputs: {
        title: 'Error de red',
        message: 'No se pudo conectar con el servidor.'
      }
    });

    expect(screen.getByText('Error de red')).toBeTruthy();
    expect(screen.getByText('No se pudo conectar con el servidor.')).toBeTruthy();
  });

  it('should emit retry event on button click', async () => {
    let retried = false;
    await render(ErrorStateComponent, {
      componentInputs: {
        showRetry: true
      },
      componentOutputs: {
        retry: {
          emit: () => { retried = true; }
        } as any
      }
    });

    const button = screen.getByRole('button', { name: /reintentar/i });
    await fireEvent.click(button);
    expect(retried).toBe(true);
  });
});
