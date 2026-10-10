import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { CheckboxModule } from 'primeng/checkbox';
import { GrcApi } from './grc-api.service';

@Component({
  selector: 'app-mcp',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CardModule, TagModule, InputTextModule, CheckboxModule],
  template: `
    <main class="grc-main">
      <h2>Servidor MCP — canal para a IA do cliente</h2>
      <p class="sub">As mesmas permissoes, trilha e travas do core. Desligado por padrao; escrita exige
        confirmacao (step-up); acoes proibidas sao sempre negadas.</p>

      <p-card>
        <div class="estado">
          <p-tag [value]="ligado() ? 'MCP ligado' : 'MCP desligado'" [severity]="ligado() ? 'success':'secondary'"></p-tag>
          <button pButton [label]="ligado() ? 'Desligar' : 'Ligar'" size="small" (click)="alternar()"></button>
        </div>
      </p-card>

      <div class="grid2">
        <p-card header="Tool: consultar risco (leitura)">
          <div class="row"><input pInputText [(ngModel)]="sujeito" placeholder="pseudonimo (P-...)" />
            <button pButton label="Consultar" icon="pi pi-search" (click)="consultar()"></button></div>
          <pre *ngIf="rConsulta()">{{ rConsulta() | json }}</pre>
        </p-card>

        <p-card header="Tool: buscar requisito (leitura)">
          <div class="row"><input pInputText [(ngModel)]="norma" placeholder="NR-35" style="max-width:120px" />
            <input pInputText [(ngModel)]="item" placeholder="35.1" style="max-width:120px" />
            <button pButton label="Buscar" icon="pi pi-book" (click)="buscar()"></button></div>
          <pre *ngIf="rReq()">{{ rReq() | json }}</pre>
        </p-card>

        <p-card header="Tool: abrir plano (escrita — exige confirmacao)">
          <div class="row"><input pInputText [(ngModel)]="pSujeito" placeholder="pseudonimo" />
            <input pInputText [(ngModel)]="pCausa" placeholder="causa (CRACHA_DESLIGADO)" /></div>
          <div class="row"><p-checkbox [(ngModel)]="confirmado" [binary]="true" inputId="cf"></p-checkbox>
            <label for="cf" class="lbl">confirmado (step-up)</label>
            <button pButton label="Abrir plano" icon="pi pi-flag" (click)="abrir()"></button></div>
          <pre *ngIf="rPlano()">{{ rPlano() | json }}</pre>
        </p-card>

        <p-card header="Acao proibida via MCP">
          <button pButton label="Tentar aceitar risco" icon="pi pi-ban" severity="danger" (click)="aceitar()"></button>
          <pre *ngIf="rAceitar()">{{ rAceitar() | json }}</pre>
        </p-card>
      </div>
    </main>
  `,
  styles: [`
    .grc-main { max-width:1140px; margin:0 auto; padding:20px; }
    h2 { font-size:1.3rem; color:var(--color-green-escuro); margin:0 0 6px; }
    .sub { color:var(--color-cinza-600,#77736E); font-size:.9rem; margin:0 0 16px; max-width:72ch; }
    .estado { display:flex; gap:12px; align-items:center; }
    .grid2 { display:grid; grid-template-columns:1fr 1fr; gap:14px; margin-top:14px; }
    .row { display:flex; gap:8px; align-items:center; margin-bottom:8px; flex-wrap:wrap; } .row input{ flex:1; min-width:120px; }
    .lbl { font-size:.85rem; margin-right:8px; }
    pre { background:#f0fbfa; border:1px solid var(--color-green-claro,#2AC4AA); border-radius:8px; padding:10px; font-size:.78rem; overflow:auto; margin:8px 0 0; }
    @media (max-width:900px){ .grid2{ grid-template-columns:1fr; } }
  `],
})
export class McpComponent implements OnInit {
  ligado = signal(false);
  sujeito = 'P-'; norma = 'NR-35'; item = '35.1'; pSujeito = 'P-'; pCausa = 'CRACHA_DESLIGADO'; confirmado = false;
  rConsulta = signal<any>(null); rReq = signal<any>(null); rPlano = signal<any>(null); rAceitar = signal<any>(null);

  constructor(private api: GrcApi) {}
  ngOnInit(): void { this.api.mcpEstado().subscribe((e) => this.ligado.set(!!e?.ligado)); }

  alternar(): void {
    (this.ligado() ? this.api.mcpDesligar() : this.api.mcpLigar()).subscribe((e) => this.ligado.set(!!e?.ligado));
  }
  consultar(): void { this.api.mcpConsultarRisco(this.sujeito).subscribe((r) => this.rConsulta.set(r)); }
  buscar(): void { this.api.mcpBuscarRequisito(this.norma, this.item).subscribe((r) => this.rReq.set(r)); }
  abrir(): void { this.api.mcpAbrirPlano(this.pSujeito, this.pCausa, this.confirmado).subscribe((r) => this.rPlano.set(r)); }
  aceitar(): void { this.api.mcpAceitarRisco().subscribe((r) => this.rAceitar.set(r)); }
}
