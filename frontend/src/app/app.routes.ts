import { Routes } from '@angular/router';
import { LandingComponent } from './landing.component';
import { PainelComponent } from './painel.component';
import { AdminComponent } from './admin.component';
import { ConformidadeComponent } from './conformidade.component';
import { AuditoriaComponent } from './auditoria.component';
import { ConferenciaComponent } from './conferencia.component';
import { McpComponent } from './mcp.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'painel', component: PainelComponent },
  { path: 'conformidade', component: ConformidadeComponent },
  { path: 'conferencia', component: ConferenciaComponent },
  { path: 'auditoria', component: AuditoriaComponent },
  { path: 'mcp', component: McpComponent },
  { path: 'admin', component: AdminComponent },
  { path: '**', redirectTo: '' },
];
