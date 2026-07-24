import { render, screen, fireEvent } from '@testing-library/angular';
import { ShellComponent } from './shell';
import { provideRouter } from '@angular/router';

describe('ShellComponent', () => {
  it('should render skip to content link', async () => {
    await render(ShellComponent, {
      providers: [provideRouter([])]
    });

    const skipLink = screen.getByRole('link', { name: /saltar al contenido/i });
    expect(skipLink).toBeTruthy();
    expect(skipLink.getAttribute('href')).toBe('#main-content');
  });

  it('should toggle mobile menu open and closed', async () => {
    const { container } = await render(ShellComponent, {
      providers: [provideRouter([])]
    });

    const toggleButton = container.querySelector('.mobile-menu-toggle') as HTMLButtonElement;
    expect(toggleButton).toBeTruthy();
    expect(toggleButton.getAttribute('aria-expanded')).toBe('false');

    await fireEvent.click(toggleButton);
    expect(toggleButton.getAttribute('aria-expanded')).toBe('true');

    await fireEvent.click(toggleButton);
    expect(toggleButton.getAttribute('aria-expanded')).toBe('false');
  });
});
