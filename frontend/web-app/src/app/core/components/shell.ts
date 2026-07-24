import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <a href="#main-content" class="skip-to-content">Saltar al contenido principal</a>

    <header class="app-header">
      <div class="header-container">
        <a routerLink="/" class="brand-logo">
          <span class="logo-icon">📚</span>
          <span class="logo-text">BookHub</span>
        </a>

        <button 
          type="button" 
          class="mobile-menu-toggle"
          [attr.aria-expanded]="isMenuOpen()"
          aria-label="Abrir menú de navegación"
          (click)="toggleMenu()">
          <span class="sr-only">Abrir menú de navegación</span>
          <span class="hamburger-icon"></span>
        </button>

        <nav class="nav-menu" [class.open]="isMenuOpen()">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" (click)="closeMenu()">Inicio</a>
          <a routerLink="/catalog" routerLinkActive="active" (click)="closeMenu()">Catálogo</a>
          <a routerLink="/library" routerLinkActive="active" (click)="closeMenu()">Mi Biblioteca</a>
          <a routerLink="/login" class="btn-login" routerLinkActive="active" (click)="closeMenu()">Ingresar</a>
        </nav>
      </div>
    </header>

    <main id="main-content" tabindex="-1" class="app-main">
      <router-outlet></router-outlet>
    </main>

    <footer class="app-footer">
      <div class="footer-container">
        <p>&copy; 2026 BookHub. Social Reading Platform.</p>
      </div>
    </footer>
  `,
  styles: [`
    .app-header {
      background-color: var(--bh-color-bg-surface);
      border-bottom: 1px solid var(--bh-color-border);
      position: sticky;
      top: 0;
      z-index: 100;
      box-shadow: var(--bh-shadow-sm);

      .header-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 12px 24px;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
    }

    .brand-logo {
      display: flex;
      align-items: center;
      gap: 8px;
      text-decoration: none;
      color: var(--bh-color-text-primary);
      font-weight: 700;
      font-size: 1.35rem;

      .logo-icon {
        font-size: 1.5rem;
      }

      .logo-text {
        letter-spacing: -0.02em;
      }
    }

    .mobile-menu-toggle {
      display: none;
      background: none;
      border: none;
      padding: 8px;
      cursor: pointer;
      color: var(--bh-color-text-primary);

      .hamburger-icon {
        display: block;
        width: 24px;
        height: 2px;
        background-color: currentColor;
        position: relative;

        &::before, &::after {
          content: '';
          position: absolute;
          width: 24px;
          height: 2px;
          background-color: currentColor;
          left: 0;
        }

        &::before { top: -6px; }
        &::after { top: 6px; }
      }

      @media (max-width: 768px) {
        display: block;
      }
    }

    .nav-menu {
      display: flex;
      align-items: center;
      gap: 20px;

      a {
        text-decoration: none;
        color: var(--bh-color-text-secondary);
        font-weight: 500;
        transition: color var(--bh-transition-fast);

        &:hover, &.active {
          color: var(--bh-color-brand-primary);
        }

        &.btn-login {
          background-color: var(--bh-color-brand-primary);
          color: #ffffff;
          padding: 6px 16px;
          border-radius: var(--bh-radius-md);

          &:hover {
            background-color: var(--bh-color-brand-hover);
            color: #ffffff;
          }
        }
      }

      @media (max-width: 768px) {
        display: none;
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        background-color: var(--bh-color-bg-surface);
        border-bottom: 1px solid var(--bh-color-border);
        flex-direction: column;
        padding: 16px;
        gap: 16px;

        &.open {
          display: flex;
        }
      }
    }

    .app-main {
      min-height: calc(100vh - 140px);
      max-width: 1200px;
      margin: 0 auto;
      padding: 24px;
    }

    .app-footer {
      border-top: 1px solid var(--bh-color-border);
      background-color: var(--bh-color-bg-surface);
      padding: 16px 24px;
      text-align: center;
      color: var(--bh-color-text-muted);
      font-size: 0.875rem;
    }
  `]
})
export class ShellComponent {
  readonly isMenuOpen = signal(false);

  toggleMenu() {
    this.isMenuOpen.update(open => !open);
  }

  closeMenu() {
    this.isMenuOpen.set(false);
  }
}
