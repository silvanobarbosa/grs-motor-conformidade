import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ToolbarModule } from 'primeng/toolbar';
import { GrcApi } from './grc-api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, ButtonModule, TableModule, CardModule, TagModule, ToolbarModule],
  template: `
    <header class="grc-header">
      <div class="grc-header-inner">
        <span class="grc-logo">Motor GRC</span>
        <span class="grc-sub">Core de risco, compliance e governanca — Demonstracao</span>
      </div>
    </header>

    <main class="grc-main">
      <p-toolbar styleClass="grc-toolbar">
        <div class="p-toolbar-group-start" style="gap:.5rem;display:flex;flex-wrap:wrap">
          <button pButton label="Carregar demo" icon="pi pi-database" (click)="carregar()" [loading]="carregando()"></button>
          <button pButton label="Avancar 30 dias" icon="pi pi-clock" severity="secondary" (click)="avancar()"></button>
          <button pButton label="Reavaliar risco" icon="pi pi-refresh" severity="secondary" (click)="reavaliar()"></button>
        </div>
        <div class="p-toolbar-group-end">
          <p-tag *ngIf="integra() !== null" [severity]="integra() ? 'success' : 'danger'"
                 [value]="integra() ? 'Trilha integra' : 'Trilha adulterada'" icon="pi pi-shield"></p-tag>
        </div>
      </p-toolbar>

      <section class="grc-cards">
        <p-card header="Cobertura">
          <div class="grc-num">{{ coberturaPct() }}<span class="grc-unit">%</span></div>
          <div class="grc-cap">requisitos obrigatorios com evidencia</div>
        </p-card>
        <p-card header="Acima do apetite">
          <div class="grc-num">{{ acimaApetite() }}</div>
          <div class="grc-cap">sujeitos com residual &gt; MEDIO</div>
        </p-card>
        <p-card header="Pendencias (15 dias)">
          <div class="grc-num">{{ pendencias().length }}</div>
          <div class="grc-cap">vencimentos a resolver</div>
        </p-card>
        <p-card header="Planos abertos">
          <div class="grc-num">{{ planosAbertos() }}</div>
          <div class="grc-cap">acoes de tratamento</div>
        </p-card>
      </section>

      <section class="grc-grid">
        <p-card header="Risco residual por sujeito" styleClass="grc-span2">
          <p-table [value]="risco()" [paginator]="true" [rows]="8" [tableStyle]="{'min-width':'40rem'}">
            <ng-template pTemplate="header">
              <tr><th>Sujeito</th><th>Nivel</th><th>Cenario</th><th>Explicacao</th></tr>
            </ng-template>
            <ng-template pTemplate="body" let-r>
              <tr>
                <td>{{ r.sujeitoId }}</td>
                <td><p-tag [severity]="cor(r.nivelResidual)" [value]="r.nivelResidual"></p-tag></td>
                <td>{{ r.cenarioDominante || '-' }}</td>
                <td class="grc-exp">{{ r.explicacao }}</td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage"><tr><td colspan="4">Carregue a demo e reavalie.</td></tr></ng-template>
          </p-table>
        </p-card>

        <p-card header="Risco por unidade">
          <table class="grc-unid">
            <tr><th>Unidade</th><th>Niveis</th></tr>
            <tr *ngFor="let u of unidades()">
              <td>{{ u.unidade }}</td>
              <td>
                <p-tag *ngFor="let n of u.niveis" [severity]="cor(n.nivel)" [value]="n.nivel + ': ' + n.qtd" styleClass="grc-mini"></p-tag>
              </td>
            </tr>
          </table>
        </p-card>
      </section>

      <section class="grc-grid">
        <p-card header="Pendencias operacionais (proximos 15 dias)" styleClass="grc-span2">
          <p-table [value]="pendencias()" [paginator]="true" [rows]="6">
            <ng-template pTemplate="header">
              <tr><th>Tipo</th><th>Sujeito</th><th>Item</th><th>Vence</th><th>Fonte</th></tr>
            </ng-template>
            <ng-template pTemplate="body" let-v>
              <tr>
                <td>{{ v.tipo }}</td><td>{{ v.pseudonimo || '-' }}</td><td>{{ v.chave || '-' }}</td>
                <td>{{ v.validoAte | date:'dd/MM/yyyy' }}</td><td>{{ v.fonte }}</td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage"><tr><td colspan="5">Sem vencimentos na janela.</td></tr></ng-template>
          </p-table>
        </p-card>
      </section>

      <footer class="grc-foot">Dados ficticios (demonstracao). Padrao Senior.</footer>
    </main>
  `,
  styles: [`
    .grc-header { background: var(--color-green-institucional); color:#fff; }
    .grc-header-inner { max-width:1140px; margin:0 auto; padding:16px 20px; display:flex; align-items:baseline; gap:14px; flex-wrap:wrap; }
    .grc-logo { font-weight:700; font-size:1.4rem; letter-spacing:.3px; }
    .grc-sub { opacity:.9; font-size:.9rem; }
    .grc-main { max-width:1140px; margin:0 auto; padding:20px; }
    .grc-toolbar { margin-bottom:18px; border:none; }
    .grc-cards { display:grid; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); gap:14px; margin-bottom:18px; }
    .grc-num { font-size:2.2rem; font-weight:700; color:var(--color-green-escuro); line-height:1; }
    .grc-unit { font-size:1rem; font-weight:500; color:var(--color-cinza-600,#77736E); }
    .grc-cap { font-size:.8rem; color:var(--color-cinza-600,#77736E); margin-top:6px; }
    .grc-grid { display:grid; grid-template-columns:2fr 1fr; gap:14px; margin-bottom:18px; }
    .grc-span2 { grid-column:auto; }
    .grc-exp { font-size:.82rem; color:var(--color-cinza-600,#77736E); max-width:28rem; }
    .grc-unid { width:100%; border-collapse:collapse; }
    .grc-unid th { text-align:left; font-size:.75rem; text-transform:uppercase; color:var(--color-cinza-600,#77736E); padding:6px 4px; }
    .grc-unid td { padding:8px 4px; border-top:1px solid var(--color-cinza-300,#D4D4D4); font-size:.9rem; }
    .grc-mini { margin:2px; }
    .grc-foot { text-align:center; color:var(--color-cinza-600,#77736E); font-size:.8rem; padding:24px 0; }
    @media (max-width:900px){ .grc-grid{ grid-template-columns:1fr; } }
  `],
})
export class AppComponent implements OnInit {
  carregando = signal(false);
  risco = signal<any[]>([]);
  pendencias = signal<any[]>([]);
  unidades = signal<{ unidade: string; niveis: { nivel: string; qtd: number }[] }[]>([]);
  coberturaPct = signal<number>(0);
  acimaApetite = signal<number>(0);
  planosAbertos = signal<number>(0);
  integra = signal<boolean | null>(null);

  constructor(private api: GrcApi) {}

  ngOnInit(): void {
    this.atualizar();
  }

  cor(nivel: string): 'success' | 'info' | 'warning' | 'danger' | 'secondary' {
    switch (nivel) {
      case 'CRITICO': return 'danger';
      case 'ALTO': return 'warning';
      case 'MEDIO': return 'info';
      case 'BAIXO': return 'secondary';
      default: return 'success';
    }
  }

  carregar(): void {
    this.carregando.set(true);
    this.api.seed(60).subscribe({
      next: () => this.api.reavaliar().subscribe(() => { this.carregando.set(false); this.atualizar(); }),
      error: () => this.carregando.set(false),
    });
  }

  avancar(): void {
    this.api.avancarDias(30).subscribe(() => this.api.reavaliar().subscribe(() => this.atualizar()));
  }

  reavaliar(): void {
    this.api.reavaliar().subscribe(() => this.atualizar());
  }

  private atualizar(): void {
    this.api.painelRisco().subscribe((r) => this.risco.set(r || []));
    this.api.operacional().subscribe((p) => this.pendencias.set(p || []));
    this.api.cobertura().subscribe((c) => this.coberturaPct.set(c?.coberturaPct ?? 0));
    this.api.executivo().subscribe((e) => this.acimaApetite.set(e?.acimaDoApetite ?? 0));
    this.api.planos().subscribe((ps) => this.planosAbertos.set((ps || []).filter((x) => x.status === 'ABERTO').length));
    this.api.integridade().subscribe((i) => this.integra.set(i?.integra ?? null));
    this.api.gestao().subscribe((g) => {
      const arr = Object.entries(g || {}).map(([unidade, niveis]: any) => ({
        unidade,
        niveis: Object.entries(niveis).map(([nivel, qtd]: any) => ({ nivel, qtd })),
      }));
      this.unidades.set(arr);
    });
  }
}
