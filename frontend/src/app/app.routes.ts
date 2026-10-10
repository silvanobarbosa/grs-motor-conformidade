import { Routes } from '@angular/router';
import { LandingComponent } from './landing.component';
import { PainelComponent } from './painel.component';
import { AdminComponent } from './admin.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'painel', component: PainelComponent },
  { path: 'admin', component: AdminComponent },
  { path: '**', redirectTo: '' },
];
