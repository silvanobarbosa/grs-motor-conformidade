import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="grc-header">
      <div class="grc-header-inner">
        <a routerLink="/" class="grc-logo">Motor GRC</a>
        <nav class="grc-nav">
          <a routerLink="/" routerLinkActive="on" [routerLinkActiveOptions]="{exact:true}">Visao geral</a>
          <a routerLink="/painel" routerLinkActive="on">Demonstracao</a>
          <a routerLink="/admin" routerLinkActive="on">Administracao</a>
        </nav>
      </div>
    </header>
    <router-outlet></router-outlet>
  `,
  styles: [`
    .grc-header { background: var(--color-green-institucional); color:#fff; }
    .grc-header-inner { max-width:1140px; margin:0 auto; padding:12px 20px; display:flex; align-items:center; gap:24px; }
    .grc-logo { font-weight:800; font-size:1.25rem; color:#fff; text-decoration:none; letter-spacing:.3px; }
    .grc-nav { display:flex; gap:18px; }
    .grc-nav a { color:#fff; opacity:.85; text-decoration:none; font-size:.92rem; font-weight:600; padding:4px 0; border-bottom:2px solid transparent; }
    .grc-nav a.on { opacity:1; border-bottom-color:var(--color-verde-neon,#12F7D1); }
    .grc-nav a:hover { opacity:1; }
  `],
})
export class AppComponent {}
