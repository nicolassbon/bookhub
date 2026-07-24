import { render, screen, fireEvent } from '@testing-library/angular';
import { LoginComponent } from './login.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { TestBed } from '@angular/core/testing';

describe('LoginComponent', () => {
  it('should render login form with email and password fields', async () => {
    await render(LoginComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    expect(screen.getByLabelText(/correo electrónico/i)).toBeTruthy();
    expect(screen.getByLabelText(/contraseña/i)).toBeTruthy();
    expect(screen.getByRole('button', { name: /ingresar/i })).toBeTruthy();
  });

  it('should disable submit button when form is invalid', async () => {
    await render(LoginComponent, {
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    const button = screen.getByRole('button', { name: /ingresar/i }) as HTMLButtonElement;
    expect(button.disabled).toBe(true);
  });
});
