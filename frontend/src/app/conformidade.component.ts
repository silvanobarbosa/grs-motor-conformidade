import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { GrcApi } from './grc-api.service';

@Component({
  selector: 'app-conformidade',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CardModule, TableModule, TagModule, InputTextModule],
  template: `
    <main class="grc-main">
      <h2>Catalogo regulatorio (consulta por data)</h2>
      <p-card>
        <div class="row">
          <input pInputText [(ngModel)]="norma" placeholder="norma (NR-35)" />
          <input pInputText [(ngModel)]="item" placeholder="item (35.1)" />
          <input pInputText [(ngModel)]="data" placeholder="data ISO (2025-03-01T00:00:00Z) — opcional" />
          <button pButton label="Buscar requisito" icon="pi pi-search" (click)="buscar()"></button>
        </div>
        <div *ngIf="req() as r" class="resultado">
          <div *ngIf="r.encontrado === false">Nenhum requisito vigente nessa data.</div>
          <div *ngIf="r.norma">
            <b>{{ r.norma }} — item {{ r.item }}</b> <p-tag [value]="'versao ' + r.versao" severity="info"></p-tag>
            <p-tag *ngIf="r.naoValidado" value="conteudo de demonstracao" severity="warning"></p-tag>
            <p class="desc">{{ r.descricao }}</p>
            <small>vigencia desde {{ r.vigenciaInicio | date:'dd/MM/yyyy' }}</small>
          </div>
        </div>
      </p-card>

      <h2>Aplicabilidade por cargo + local</h2>
      <p-card>
        <div class="row">
          <input pInputText [(ngModel)]="cargo" placeholder="cargo (ELETRICISTA)" />
          <input pInputText [(ngModel)]="local" placeholder="local (PLANTA_NORTE)" />
          <button pButton label="Sugerir normas" icon="pi pi-list" (click)="sugerir()"></button>
        </div>
        <p-table [value]="sugestoes()" *ngIf="sugestoes().length">
          <ng-template pTemplate="header"><tr><th>Norma</th><th>Resultado</th><th>Criticidade</th></tr></ng-template>
          <ng-template pTemplate="body" let-s>
            <tr><td>{{ s.normaCodigo }}</td>
              <td><p-tag [value]="s.resultado" [severity]="s.resultado==='OBRIGATORIO'?'danger':(s.resultado==='REQUER_CONFIRMACAO'?'warning':'secondary')"></p-tag></td>
              <td>{{ s.altaCriticidade ? 'alta' : '-' }}</td></tr>
          </ng-template>
        </p-table>
      </p-card>

      <h2>Politicas internas (governanca)</h2>
      <p-card>
        <div class="row">
          <input pInputText [(ngModel)]="pCod" placeholder="codigo (POL-CRACHA)" />
          <input pInputText [(ngModel)]="pTit" placeholder="titulo" />
          <input pInputText [(ngModel)]="pCargo" placeholder="cargo alvo (VIGILANTE)" />
          <input pInputText [(ngModel)]="pLocal" placeholder="local alvo (opcional)" />
          <button pButton label="Criar" icon="pi pi-plus" severity="secondary" (click)="criar()"></button>
        </div>
        <p-table [value]="politicas()">
          <ng-template pTemplate="header"><tr><th>Codigo</th><th>Titulo</th><th>Status</th><th>Alvo</th><th></th></tr></ng-template>
          <ng-template pTemplate="body" let-p>
            <tr>
              <td>{{ p.codigo }}</td><td>{{ p.titulo }}</td>
              <td><p-tag [value]="p.status" [severity]="p.status==='PUBLICADA'?'success':'secondary'"></p-tag></td>
              <td>{{ p.cargoAlvo }}{{ p.localAlvo ? ' @ ' + p.localAlvo : '' }}</td>
              <td>
                <button *ngIf="p.status!=='PUBLICADA'" pButton label="Publicar" size="small" (click)="publicar(p.codigo)"></button>
                <button *ngIf="p.status==='PUBLICADA'" pButton label="Pendencias" size="small" severity="secondary" (click)="pend(p.codigo)"></button>
              </td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage"><tr><td colspan="5">Sem politicas. Crie uma acima.</td></tr></ng-template>
        </p-table>
        <div *ngIf="pendencias() as pd" class="resultado">
          <b>Pendencias de ciencia ({{ pdCod() }}):</b> {{ pd.length }} pessoa(s)
          <p-tag *ngFor="let x of pd" [value]="x" styleClass="grc-mini"></p-tag>
          <span *ngIf="!pd.length"> — todos cientes.</span>
        </div>
      </p-card>
    </main>
  `,
  styles: [`
    .grc-main { max-width:1140px; margin:0 auto; padding:20px; }
    h2 { font-size:1.3rem; color:var(--color-green-escuro); margin:22px 0 10px; }
    .row { display:flex; gap:8px; flex-wrap:wrap; margin-bottom:12px; } .row input{ flex:1; min-width:140px; }
    .resultado { margin-top:12px; font-size:.9rem; } .desc{ margin:8px 0 4px; } small{ color:var(--color-cinza-600,#77736E); }
    .grc-mini { margin:2px; }
  `],
})
export class ConformidadeComponent implements OnInit {
  norma = 'NR-35'; item = '35.1'; data = '2025-03-01T00:00:00Z';
  cargo = 'ELETRICISTA'; local = 'PLANTA_NORTE';
  pCod = 'POL-CRACHA'; pTit = 'Cracha de desligado bloqueado em 24h'; pCargo = 'VIGILANTE'; pLocal = '';
  req = signal<any>(null);
  sugestoes = signal<any[]>([]);
  politicas = signal<any[]>([]);
  pendencias = signal<string[] | null>(null);
  pdCod = signal('');

  constructor(private api: GrcApi) {}
  ngOnInit(): void { this.carregarPoliticas(); }

  buscar(): void { this.api.requisito(this.norma, this.item, this.data?.trim() || undefined).subscribe((r) => this.req.set(r)); }
  sugerir(): void { this.api.sugerir(this.cargo, this.local).subscribe((s) => this.sugestoes.set(s || [])); }
  carregarPoliticas(): void { this.api.politicas().subscribe((p) => this.politicas.set(p || [])); }
  criar(): void { this.api.criarPolitica({ codigo: this.pCod, titulo: this.pTit, cargoAlvo: this.pCargo, localAlvo: this.pLocal || null }).subscribe(() => this.carregarPoliticas()); }
  publicar(cod: string): void { this.api.publicarPolitica(cod).subscribe(() => this.carregarPoliticas()); }
  pend(cod: string): void { this.pdCod.set(cod); this.api.pendenciasCiencia(cod).subscribe((p) => this.pendencias.set(p || [])); }
}
