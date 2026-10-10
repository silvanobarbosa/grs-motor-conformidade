import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { GrcApi } from './grc-api.service';

@Component({
  selector: 'app-conferencia',
  standalone: true,
  imports: [CommonModule, ButtonModule, CardModule, TableModule, TagModule],
  template: `
    <main class="grc-main">
      <h2>Conferencia de evidencias (OCR de baixa confianca)</h2>
      <p class="sub">Documento lido pelo OCR com campo duvidoso entra como <b>nao conferido</b> e NAO reduz o risco
        ate a conferencia humana. Depois de conferir, a evidencia vale.</p>

      <p-card>
        <div class="row">
          <button pButton label="Simular OCR (doc duvidoso)" icon="pi pi-file-import" (click)="gerar()" [loading]="gerando()"></button>
          <button pButton label="Atualizar" icon="pi pi-refresh" severity="secondary" (click)="carregar()"></button>
        </div>
        <p-table [value]="pendentes()">
          <ng-template pTemplate="header"><tr><th>Empresa</th><th>Documento</th><th>Fonte</th><th>Confianca</th><th></th></tr></ng-template>
          <ng-template pTemplate="body" let-e>
            <tr>
              <td>{{ e.empresa }}</td><td>{{ e.documento }}</td><td>{{ e.sistemaOrigem }}</td>
              <td><p-tag value="BAIXA" severity="warning"></p-tag></td>
              <td><button pButton label="Conferir" icon="pi pi-check" size="small" (click)="conferir(e.id)"></button></td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage"><tr><td colspan="5">Nada pendente de conferencia.</td></tr></ng-template>
        </p-table>
      </p-card>
    </main>
  `,
  styles: [`
    .grc-main { max-width:1140px; margin:0 auto; padding:20px; }
    h2 { font-size:1.3rem; color:var(--color-green-escuro); margin:0 0 6px; }
    .sub { color:var(--color-cinza-600,#77736E); font-size:.9rem; margin:0 0 16px; max-width:70ch; }
    .row { display:flex; gap:8px; margin-bottom:12px; flex-wrap:wrap; }
  `],
})
export class ConferenciaComponent implements OnInit {
  pendentes = signal<any[]>([]);
  gerando = signal(false);

  constructor(private api: GrcApi) {}
  ngOnInit(): void { this.carregar(); }

  carregar(): void { this.api.conferenciaPendentes().subscribe((p) => this.pendentes.set(p || [])); }
  gerar(): void {
    this.gerando.set(true);
    this.api.ocrTerceiro('TERC-OBRA', 'ASO', 90, true).subscribe(() => { this.gerando.set(false); this.carregar(); });
  }
  conferir(id: string): void { this.api.conferir(id).subscribe(() => this.carregar()); }
}
