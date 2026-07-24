import { render, screen } from '@testing-library/angular';
import { RegisterComponent } from './register';
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

    expect(screen.getByText('Crear una Cuenta en BookHub')).toBeTruthy();
    expect(screen.getByLabelText(/Nombre Visible/i)).toBeTruthy();
    expect(screen.getByLabelText(/Correo Electrónico/i)).toBeTruthy();
    expect(screen.getByLabelText(/Contraseña/i)).toBeTruthy();
  });
});
