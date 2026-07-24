import { render, screen } from '@testing-library/angular';
import { RegisterComponent } from './register.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

describe('RegisterComponent', () => {
  it('should render registration fields', async () => {
    await render(RegisterComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    expect(screen.getByLabelText(/nombre visible/i)).toBeTruthy();
    expect(screen.getByLabelText(/correo electrónico/i)).toBeTruthy();
    expect(screen.getByLabelText(/contraseña/i)).toBeTruthy();
    expect(screen.getByRole('button', { name: /registrarse/i })).toBeTruthy();
  });
});
