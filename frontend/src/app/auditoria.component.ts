import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { GrcApi } from './grc-api.service';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CardModule, TableModule, TagModule, InputTextModule],
  template: `
    <main class="grc-main">
      <h2>Auditoria externa e dossie de conformidade</h2>
      <p-card header="1. Conceder acesso ao auditor">
        <div class="row">
          <input pInputText [(ngModel)]="auditor" placeholder="auditor (auditor-1)" />
          <input pInputText [(ngModel)]="escopo" placeholder="escopo (NR-35)" />
          <input pInputText type="number" [(ngModel)]="dias" placeholder="dias" style="max-width:120px" />
          <button pButton label="Conceder acesso" icon="pi pi-key" (click)="conceder()"></button>
        </div>
        <p *ngIf="concedido()" class="ok">Acesso concedido ao {{ auditor }} (escopo {{ escopo }}), temporario e so leitura.</p>
      </p-card>

      <p-card header="2. Gerar dossie (pseudonimizado, com prova de integridade)">
        <div class="row">
          <input pInputText [(ngModel)]="norma" placeholder="norma (NR-35)" />
          <button pButton label="Gerar dossie" icon="pi pi-file" (click)="gerar()"></button>
        </div>
        <p *ngIf="erro()" class="erro">{{ erro() }}</p>
        <div *ngIf="dossie() as d" class="dossie">
          <div class="selos">
            <p-tag [value]="d.itens + ' itens'" severity="info"></p-tag>
            <p-tag [value]="d.trilhaIntegra ? 'trilha integra' : 'trilha adulterada'" [severity]="d.trilhaIntegra ? 'success':'danger'" icon="pi pi-shield"></p-tag>
            <span class="hash">hash: {{ d.hashDossie?.slice(0,24) }}…</span>
          </div>
          <p-table [value]="d.conteudo" [paginator]="true" [rows]="8">
            <ng-template pTemplate="header"><tr><th>Pseudonimo</th><th>Tipo</th><th>Fato</th><th>Vence</th><th>Fonte</th></tr></ng-template>
            <ng-template pTemplate="body" let-i>
              <tr><td>{{ i.pseudonimo || '-' }}</td><td>{{ i.tipo }}</td><td class="fato">{{ i.fato }}</td>
                <td>{{ i.validoAte | date:'dd/MM/yyyy' }}</td><td>{{ i.fonte }}</td></tr>
            </ng-template>
            <ng-template pTemplate="emptymessage"><tr><td colspan="5">Sem itens dessa norma.</td></tr></ng-template>
          </p-table>
          <p class="nota">Pseudonimizado por padrao: o dossie nunca mostra CPF.</p>
        </div>
      </p-card>
    </main>
  `,
  styles: [`
    .grc-main { max-width:1140px; margin:0 auto; padding:20px; }
    h2 { font-size:1.3rem; color:var(--color-green-escuro); margin:0 0 14px; }
    .row { display:flex; gap:8px; flex-wrap:wrap; margin-bottom:10px; } .row input{ flex:1; min-width:140px; }
    .ok { color:var(--color-green-escuro); font-size:.88rem; } .erro{ color:var(--color-pink,#FF0069); font-size:.85rem; }
    .selos { display:flex; gap:10px; align-items:center; margin:8px 0 12px; flex-wrap:wrap; }
    .hash { font-family:monospace; font-size:.78rem; color:var(--color-cinza-600,#77736E); }
    .fato { font-size:.82rem; color:var(--color-cinza-600,#77736E); max-width:30rem; }
    .nota { font-size:.78rem; color:var(--color-cinza-600,#77736E); margin-top:8px; }
  `],
})
export class AuditoriaComponent {
  auditor = 'auditor-1'; escopo = 'NR-35'; dias = 5; norma = 'NR-35';
  concedido = signal(false);
  dossie = signal<any>(null);
  erro = signal('');

  constructor(private api: GrcApi) {}

  conceder(): void { this.api.concederAcesso({ auditor: this.auditor, escopo: this.escopo, dias: this.dias, por: 'key-user' }).subscribe(() => this.concedido.set(true)); }
  gerar(): void {
    this.erro.set('');
    this.api.gerarDossie(this.auditor, this.norma).subscribe({
      next: (d) => this.dossie.set(d),
      error: () => this.erro.set('Conceda o acesso ao auditor antes de gerar o dossie.'),
    });
  }
}
