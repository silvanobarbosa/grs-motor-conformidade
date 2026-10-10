import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { marked } from 'marked';
import { GrcApi } from './grc-api.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CardModule, TableModule, TagModule, InputTextModule],
  template: `
    <main class="grc-main">
      <!-- LOGIN -->
      <div *ngIf="!token()" class="login-wrap">
        <p-card header="Area administrativa" styleClass="login-card">
          <p class="hint">Entre com o admin master para gerir usuarios e ver a observabilidade do sistema.</p>
          <div class="field"><label>Login</label><input pInputText [(ngModel)]="login" placeholder="admin.master" /></div>
          <div class="field"><label>Senha</label><input pInputText type="password" [(ngModel)]="senha" (keyup.enter)="entrar()" /></div>
          <button pButton label="Entrar" icon="pi pi-sign-in" (click)="entrar()" [loading]="entrando()"></button>
          <p *ngIf="erro()" class="erro">{{ erro() }}</p>
          <p class="demo-cred">Demo: <code>admin.master</code> / <code>AdminMaster&#64;2026</code></p>
        </p-card>
      </div>

      <!-- PAINEL ADMIN -->
      <div *ngIf="token()">
        <div class="admin-top">
          <div><b>{{ nome() }}</b> <p-tag *ngFor="let p of papeis()" [value]="p" severity="info" styleClass="grc-mini"></p-tag></div>
          <button pButton label="Sair" icon="pi pi-sign-out" severity="secondary" size="small" (click)="sair()"></button>
        </div>

        <!-- OBSERVABILIDADE -->
        <h2>Observabilidade do sistema</h2>
        <section class="grc-cards" *ngIf="obs() as o">
          <p-card header="Eventos na trilha"><div class="grc-num">{{ o.eventosTrilha }}</div>
            <p-tag [severity]="o.trilhaIntegra ? 'success':'danger'" [value]="o.trilhaIntegra ? 'integra':'adulterada'"></p-tag></p-card>
          <p-card header="Evidencias"><div class="grc-num">{{ o.evidencias }}</div><div class="grc-cap">{{ o.evidenciasNaoConferidas }} a conferir</div></p-card>
          <p-card header="Pessoas (cofre)"><div class="grc-num">{{ o.pessoas }}</div><div class="grc-cap">colaboradores</div></p-card>
          <p-card header="Usuarios do sistema"><div class="grc-num">{{ o.usuariosSistema }}</div><div class="grc-cap">operadores</div></p-card>
          <p-card header="Planos"><div class="grc-num">{{ o.planosAbertos }}</div><div class="grc-cap">{{ o.planosConcluidos }} concluidos</div></p-card>
          <p-card header="Cobertura"><div class="grc-num">{{ o.cobertura?.coberturaPct }}%</div><div class="grc-cap">requisitos c/ evidencia</div></p-card>
          <p-card header="IA (Sara)"><p-tag [severity]="o.iaLigada ? 'success':'secondary'" [value]="o.iaLigada ? 'ligada':'desligada'"></p-tag></p-card>
          <p-card header="MCP"><p-tag [severity]="o.mcpLigado ? 'success':'secondary'" [value]="o.mcpLigado ? 'ligado':'desligado'"></p-tag></p-card>
        </section>

        <section class="grc-grid2" *ngIf="obs() as o">
          <p-card header="Risco por nivel">
            <p-tag *ngFor="let k of niveis(o.riscoPorNivel)" [value]="k.nivel + ': ' + k.qtd" [severity]="cor(k.nivel)" styleClass="grc-mini"></p-tag>
          </p-card>
          <p-card header="Atualidade por fonte">
            <table class="mini"><tr *ngFor="let f of fontes(o.atualidadePorFonte)"><td>{{ f.fonte }}</td><td class="r">{{ f.quando | date:'dd/MM HH:mm' }}</td></tr></table>
          </p-card>
        </section>

        <!-- GESTAO DE USUARIOS -->
        <h2>Gestao de usuarios</h2>
        <p-card>
          <div class="novo">
            <input pInputText [(ngModel)]="nLogin" placeholder="login" />
            <input pInputText [(ngModel)]="nNome" placeholder="nome" />
            <input pInputText type="password" [(ngModel)]="nSenha" placeholder="senha (min 8)" />
            <input pInputText [(ngModel)]="nPapeis" placeholder="papeis (ex: GESTOR_RISCOS,TST)" />
            <button pButton label="Criar usuario" icon="pi pi-user-plus" (click)="criar()"></button>
          </div>
          <p *ngIf="erroUsuario()" class="erro">{{ erroUsuario() }}</p>
          <p-table [value]="usuarios()" [tableStyle]="{'min-width':'30rem'}">
            <ng-template pTemplate="header"><tr><th>Login</th><th>Nome</th><th>Papeis</th><th>Status</th><th></th></tr></ng-template>
            <ng-template pTemplate="body" let-u>
              <tr>
                <td>{{ u.login }}</td><td>{{ u.nome }}</td>
                <td><p-tag *ngFor="let p of u.papeis" [value]="p" severity="info" styleClass="grc-mini"></p-tag></td>
                <td><p-tag [severity]="u.ativo ? 'success':'secondary'" [value]="u.ativo ? 'ativo':'inativo'"></p-tag></td>
                <td><button *ngIf="u.login !== 'admin.master'" pButton [label]="u.ativo ? 'Desativar':'Ativar'" size="small"
                            [severity]="u.ativo ? 'danger':'success'" (click)="alternar(u)"></button></td>
              </tr>
            </ng-template>
          </p-table>
        </p-card>

        <!-- DOCUMENTACAO -->
        <h2>Documentacao do sistema</h2>
        <p-card>
          <button *ngIf="!docHtml()" pButton label="Abrir documentacao" icon="pi pi-book" (click)="carregarDoc()" [loading]="docCarregando()"></button>
          <div *ngIf="docHtml() as h" class="doc" [innerHTML]="h"></div>
        </p-card>
      </div>
    </main>
  `,
  styles: [`
    .grc-main { max-width:1140px; margin:0 auto; padding:20px; }
    h2 { font-size:1.3rem; color:var(--color-green-escuro); margin:22px 0 10px; }
    .login-wrap { display:flex; justify-content:center; padding:40px 0; }
    .login-card { width:380px; max-width:92vw; }
    .hint { font-size:.85rem; color:var(--color-cinza-600,#77736E); }
    .field { display:flex; flex-direction:column; gap:4px; margin-bottom:12px; }
    .field label { font-size:.78rem; font-weight:600; color:var(--color-cinza-600,#77736E); }
    .field input { width:100%; }
    .erro { color:var(--color-pink,#FF0069); font-size:.82rem; margin-top:8px; }
    .demo-cred { font-size:.78rem; color:var(--color-cinza-600,#77736E); margin-top:12px; }
    .admin-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
    .grc-cards { display:grid; grid-template-columns:repeat(auto-fit,minmax(170px,1fr)); gap:12px; }
    .grc-num { font-size:2rem; font-weight:700; color:var(--color-green-escuro); line-height:1; }
    .grc-cap { font-size:.78rem; color:var(--color-cinza-600,#77736E); margin-top:4px; }
    .grc-grid2 { display:grid; grid-template-columns:1fr 1fr; gap:12px; margin-top:14px; }
    .grc-mini { margin:2px; }
    .mini { width:100%; font-size:.85rem; } .mini td{ padding:4px 0; border-top:1px solid var(--color-cinza-300,#D4D4D4);} .mini .r{ text-align:right; color:var(--color-cinza-600,#77736E);}
    .novo { display:flex; gap:8px; flex-wrap:wrap; margin-bottom:14px; }
    .novo input { flex:1; min-width:140px; }
    .doc { font-size:.9rem; line-height:1.6; color:var(--color-cinza-900,#3B3D3D); max-width:80ch; }
    .doc h1,.doc h2,.doc h3 { color:var(--color-green-escuro); margin:18px 0 8px; }
    .doc h1 { font-size:1.5rem; } .doc h2 { font-size:1.2rem; } .doc h3 { font-size:1.02rem; }
    .doc table { border-collapse:collapse; width:100%; margin:10px 0; display:block; overflow-x:auto; }
    .doc th,.doc td { border:1px solid var(--color-cinza-300,#D4D4D4); padding:6px 10px; text-align:left; font-size:.85rem; }
    .doc th { background:#f0fbfa; }
    .doc code { background:var(--color-cinza-100,#F2F2F2); padding:1px 5px; border-radius:4px; font-size:.85em; }
    .doc pre { background:#f0fbfa; border:1px solid var(--color-green-claro,#2AC4AA); border-radius:8px; padding:12px; overflow-x:auto; }
    .doc pre code { background:none; padding:0; }
    .doc a { color:var(--color-green-institucional); }
    @media (max-width:900px){ .grc-grid2{ grid-template-columns:1fr; } }
  `],
})
export class AdminComponent {
  login = 'admin.master';
  senha = '';
  token = signal<string | null>(null);
  nome = signal('');
  papeis = signal<string[]>([]);
  entrando = signal(false);
  erro = signal('');
  obs = signal<any>(null);
  usuarios = signal<any[]>([]);
  erroUsuario = signal('');
  nLogin = ''; nNome = ''; nSenha = ''; nPapeis = '';
  docHtml = signal<string | null>(null);
  docCarregando = signal(false);

  constructor(private api: GrcApi, private http: HttpClient) {}

  carregarDoc(): void {
    this.docCarregando.set(true);
    this.http.get('DOCUMENTACAO.md', { responseType: 'text' }).subscribe({
      next: (md) => { this.docHtml.set(marked.parse(md) as string); this.docCarregando.set(false); },
      error: () => { this.docHtml.set('<p>Documentacao indisponivel.</p>'); this.docCarregando.set(false); },
    });
  }

  cor(nivel: string): 'success' | 'info' | 'warning' | 'danger' | 'secondary' {
    switch (nivel) { case 'CRITICO': return 'danger'; case 'ALTO': return 'warning'; case 'MEDIO': return 'info'; case 'BAIXO': return 'secondary'; default: return 'success'; }
  }
  niveis(m: any) { return Object.entries(m || {}).map(([nivel, qtd]: any) => ({ nivel, qtd })); }
  fontes(m: any) { return Object.entries(m || {}).map(([fonte, quando]: any) => ({ fonte, quando })); }

  entrar(): void {
    this.entrando.set(true); this.erro.set('');
    this.api.adminLogin(this.login, this.senha).subscribe({
      next: (r: any) => { this.entrando.set(false); this.token.set(r.token); this.nome.set(r.nome); this.papeis.set(r.papeis); this.carregar(); },
      error: () => { this.entrando.set(false); this.erro.set('Credenciais invalidas.'); },
    });
  }

  sair(): void { this.token.set(null); this.senha = ''; }

  private carregar(): void {
    const t = this.token()!;
    this.api.adminObservabilidade(t).subscribe((o) => this.obs.set(o));
    this.api.adminUsuarios(t).subscribe((u) => this.usuarios.set(u || []));
  }

  criar(): void {
    this.erroUsuario.set('');
    const papeis = this.nPapeis.split(',').map((p) => p.trim()).filter(Boolean);
    this.api.adminCriarUsuario(this.token()!, { login: this.nLogin, nome: this.nNome, senha: this.nSenha, papeis }).subscribe({
      next: () => { this.nLogin = this.nNome = this.nSenha = this.nPapeis = ''; this.carregar(); },
      error: (e) => this.erroUsuario.set(e?.error?.erro || 'Nao foi possivel criar o usuario.'),
    });
  }

  alternar(u: any): void {
    this.api.adminAtivo(this.token()!, u.id, !u.ativo).subscribe(() => this.carregar());
  }
}
