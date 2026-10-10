import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <!-- HERO -->
    <section class="hero">
      <div class="wrap hero-inner">
        <div class="hero-text">
          <span class="eyebrow">ISO 31000 · ISO 37301 · LGPD</span>
          <h1>O sistema nervoso da gestao de riscos</h1>
          <p>O Motor GRC e um core unico que puxa evidencias dos sistemas da empresa e vira a fonte de
            verdade para <b>risco</b>, <b>compliance</b> e <b>governanca</b>. Os sistemas operacionais
            continuam registrando; o Motor GRC e a inteligencia de risco em cima deles.</p>
          <div class="cta">
            <a routerLink="/painel" class="btn btn-primary">Ver a demonstracao</a>
            <a routerLink="/admin" class="btn btn-ghost">Area administrativa</a>
          </div>
          <p class="nota">Versao de demonstracao, com dados ficticios. Padrao Senior.</p>
        </div>
        <div class="hero-badges">
          <div class="badge"><span class="bignum">4</span>sistemas de origem conectaveis</div>
          <div class="badge"><span class="bignum">8</span>cenarios de risco prontos</div>
          <div class="badge"><span class="bignum">100%</span>trilha auditavel por hash</div>
        </div>
      </div>
    </section>

    <!-- O QUE FAZ -->
    <section class="wrap bloco">
      <h2>O que o sistema faz</h2>
      <div class="cards4">
        <div class="feat"><div class="ico">◆</div><h3>Mede risco residual</h3>
          <p>Calcula o risco automaticamente pela eficacia dos controles, a partir das evidencias. Nunca editado a mao.</p></div>
        <div class="feat"><div class="ico">§</div><h3>Vigia a conformidade</h3>
          <p>Catalogo de normas (NRs) versionado, com aplicabilidade por cargo e local e consulta "cumpria a regra na data?".</p></div>
        <div class="feat"><div class="ico">⚑</div><h3>Abre e cobra acoes</h3>
          <p>Quando o risco sobe, abre um plano de acao e atribui o dono por regra (cargo + local), fechando o ciclo.</p></div>
        <div class="feat"><div class="ico">⛉</div><h3>Prova e audita</h3>
          <p>Trilha so-inclusao encadeada por hash, dossie pseudonimizado para auditores e indicadores que abrem ate a evidencia.</p></div>
      </div>
    </section>

    <!-- COMO FUNCIONA (INFOGRAFICO) -->
    <section class="bloco-cinza">
      <div class="wrap">
        <h2>Como funciona</h2>
        <p class="sub">Toda evidencia entra por uma unica porta. O core processa, explica e age. A exibicao respeita as permissoes.</p>

        <div class="flow">
          <div class="col">
            <div class="col-label">Sistemas de origem</div>
            <div class="node src">RH</div>
            <div class="node src">Controle de acesso</div>
            <div class="node src">Gestao de terceiros</div>
            <div class="node src">OCR · GED · Sign · BPM</div>
          </div>

          <div class="arrow">→</div>

          <div class="col">
            <div class="col-label">Porta de entrada</div>
            <div class="node porta">Contrato unico de evidencia<small>quem · o que · quando · de onde · validade · confianca</small></div>
          </div>

          <div class="arrow">→</div>

          <div class="col">
            <div class="col-label">Core de inteligencia</div>
            <div class="node core">Trilha imutavel (hash)</div>
            <div class="node core">Motor de avaliacao + travas</div>
            <div class="node core">Catalogo regulatorio</div>
            <div class="node core">Cofre de identidade (LGPD)</div>
          </div>

          <div class="arrow">→</div>

          <div class="col">
            <div class="col-label">Saidas</div>
            <div class="node out">Risco residual explicavel</div>
            <div class="node out">Planos de acao (donos)</div>
            <div class="node out">Indicadores e dossie</div>
            <div class="node out">MCP para a IA do cliente</div>
          </div>
        </div>

        <div class="legenda">
          <span><i class="dot dot-src"></i> fontes</span>
          <span><i class="dot dot-porta"></i> porta unica</span>
          <span><i class="dot dot-core"></i> core</span>
          <span><i class="dot dot-out"></i> saidas</span>
        </div>
      </div>
    </section>

    <!-- PRINCIPIOS -->
    <section class="wrap bloco">
      <h2>Principios inegociaveis</h2>
      <ul class="princ">
        <li><b>Trilha imutavel.</b> Nada e editado nem apagado; correcao e um novo evento, encadeado por hash.</li>
        <li><b>Risco residual derivado.</b> E sempre resultado do processamento das evidencias — nunca editado.</li>
        <li><b>Toda mudanca de risco e explicavel:</b> qual evidencia, de qual fonte, em qual regra.</li>
        <li><b>Dado pessoal so no cofre.</b> Trilha e indicadores guardam so o pseudonimo; CPF nunca sai.</li>
        <li><b>A IA nunca decide.</b> Sugere, extrai e explica. O produto funciona com a IA desligada.</li>
        <li><b>Multi-tenant e permissoes:</b> o core processa tudo; a exibicao respeita papel, escopo e categoria.</li>
      </ul>
      <div class="cta center">
        <a routerLink="/painel" class="btn btn-primary">Abrir a demonstracao</a>
      </div>
    </section>

    <footer class="foot">Motor GRC — demonstracao com dados ficticios · Padrao Senior</footer>
  `,
  styles: [`
    .wrap { max-width:1140px; margin:0 auto; padding:0 20px; }
    h2 { font-size:1.6rem; color:var(--color-green-escuro); margin:0 0 6px; }
    .bloco { padding:48px 20px; }
    .bloco-cinza { padding:48px 0; background:#fff; border-top:1px solid var(--color-cinza-300,#D4D4D4); border-bottom:1px solid var(--color-cinza-300,#D4D4D4); }
    .sub { color:var(--color-cinza-600,#77736E); margin:0 0 26px; }

    .hero { background:linear-gradient(135deg, var(--color-green-institucional), var(--color-green-escuro)); color:#fff; }
    .hero-inner { display:grid; grid-template-columns:1.4fr .9fr; gap:32px; padding:56px 20px; align-items:center; }
    .eyebrow { text-transform:uppercase; letter-spacing:1.5px; font-size:.72rem; font-weight:700; opacity:.85; }
    .hero h1 { font-size:2.6rem; line-height:1.1; margin:12px 0 14px; max-width:16ch; }
    .hero p { font-size:1.05rem; line-height:1.55; opacity:.95; max-width:52ch; }
    .cta { display:flex; gap:12px; margin-top:22px; flex-wrap:wrap; }
    .cta.center { justify-content:center; margin-top:28px; }
    .btn { display:inline-block; padding:12px 22px; border-radius:10px; font-weight:700; text-decoration:none; font-size:.95rem; }
    .btn-primary { background:#fff; color:var(--color-green-escuro); }
    .btn-ghost { border:2px solid rgba(255,255,255,.6); color:#fff; }
    .nota { font-size:.78rem; opacity:.8; margin-top:14px; }
    .hero-badges { display:flex; flex-direction:column; gap:12px; }
    .badge { background:rgba(255,255,255,.12); border:1px solid rgba(255,255,255,.25); border-radius:12px; padding:16px; font-size:.85rem; }
    .bignum { display:block; font-size:2rem; font-weight:800; line-height:1; margin-bottom:4px; color:var(--color-verde-neon,#12F7D1); }

    .cards4 { display:grid; grid-template-columns:repeat(auto-fit,minmax(230px,1fr)); gap:16px; }
    .feat { background:#fff; border:1px solid var(--color-cinza-300,#D4D4D4); border-radius:14px; padding:20px; }
    .feat .ico { width:40px; height:40px; border-radius:10px; background:var(--color-green-claro,#2AC4AA); color:#fff; display:flex; align-items:center; justify-content:center; font-size:1.3rem; margin-bottom:12px; }
    .feat h3 { margin:0 0 6px; font-size:1.05rem; color:var(--color-green-escuro); }
    .feat p { margin:0; font-size:.88rem; color:var(--color-cinza-600,#77736E); line-height:1.5; }

    .flow { display:grid; grid-template-columns:1fr auto 1fr auto 1fr auto 1fr; gap:10px; align-items:stretch; }
    .col { display:flex; flex-direction:column; gap:8px; }
    .col-label { font-size:.7rem; text-transform:uppercase; letter-spacing:1px; color:var(--color-cinza-600,#77736E); font-weight:700; margin-bottom:2px; }
    .node { border-radius:10px; padding:12px; font-size:.85rem; font-weight:600; border:1px solid; background:#fff; }
    .node small { display:block; font-weight:400; font-size:.7rem; color:var(--color-cinza-600,#77736E); margin-top:4px; }
    .node.src { border-color:var(--color-azul-medio,#8EA0C5); color:var(--color-azul-escuro,#33466C); }
    .node.porta { border-color:var(--color-laranja,#EF8833); color:#8a4a12; background:#fff7f0; }
    .node.core { border-color:var(--color-green-institucional); color:var(--color-green-escuro); background:#f0fbfa; }
    .node.out { border-color:var(--color-roxo,#844EC3); color:#4a2d73; background:#f8f4fd; }
    .arrow { display:flex; align-items:center; justify-content:center; font-size:1.6rem; color:var(--color-green-medio,#00AF9B); font-weight:700; }
    .legenda { display:flex; gap:18px; flex-wrap:wrap; margin-top:20px; font-size:.78rem; color:var(--color-cinza-600,#77736E); }
    .dot { display:inline-block; width:10px; height:10px; border-radius:50%; margin-right:5px; vertical-align:middle; }
    .dot-src{background:#8EA0C5}.dot-porta{background:#EF8833}.dot-core{background:#00968C}.dot-out{background:#844EC3}

    .princ { list-style:none; padding:0; margin:0; display:grid; grid-template-columns:1fr 1fr; gap:12px; }
    .princ li { background:#fff; border:1px solid var(--color-cinza-300,#D4D4D4); border-left:4px solid var(--color-green-institucional); border-radius:10px; padding:14px 16px; font-size:.9rem; line-height:1.5; }
    .princ b { color:var(--color-green-escuro); }

    .foot { text-align:center; color:var(--color-cinza-600,#77736E); font-size:.8rem; padding:28px 20px; }

    @media (max-width:960px){
      .hero-inner{ grid-template-columns:1fr; } .hero h1{ font-size:2rem; }
      .flow{ grid-template-columns:1fr; } .arrow{ transform:rotate(90deg); }
      .princ{ grid-template-columns:1fr; }
    }
  `],
})
export class LandingComponent {}
