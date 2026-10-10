import { ApplicationConfig } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter, withHashLocation } from '@angular/router';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),
    provideAnimations(),
    // Hash location: evita 404 em refresh de rota no hosting estatico (Vercel).
    provideRouter(routes, withHashLocation()),
  ],
};
