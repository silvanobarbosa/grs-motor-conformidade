import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * Cliente da API do Motor GRC. A base da API vem de uma config em runtime (window.__GRC_API__),
 * com fallback para localhost no desenvolvimento. O tenant da demo vai no cabecalho X-Tenant-Id.
 */
@Injectable({ providedIn: 'root' })
export class GrcApi {
  private readonly base: string =
    (window as any).__GRC_API__ || 'http://localhost:8080';
  private readonly tenant = 'metalurgica-exemplo';

  constructor(private http: HttpClient) {}

  private get headers(): HttpHeaders {
    return new HttpHeaders({ 'X-Tenant-Id': this.tenant });
  }

  seed(colaboradores = 120): Observable<any> {
    return this.http.post(`${this.base}/api/demo/seed?colaboradores=${colaboradores}`, {}, { headers: this.headers });
  }

  avancarDias(dias: number): Observable<any> {
    return this.http.post(`${this.base}/api/demo/relogio/avancar?dias=${dias}`, {}, { headers: this.headers });
  }

  reavaliar(): Observable<any> {
    return this.http.post(`${this.base}/api/risco/reavaliar`, {}, { headers: this.headers });
  }

  painelRisco(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/api/risco`, { headers: this.headers });
  }

  gestao(): Observable<any> {
    return this.http.get(`${this.base}/api/indicadores/gestao`, { headers: this.headers });
  }

  cobertura(): Observable<any> {
    return this.http.get(`${this.base}/api/indicadores/cobertura`, { headers: this.headers });
  }

  executivo(apetite = 'MEDIO'): Observable<any> {
    return this.http.get(`${this.base}/api/indicadores/executivo?apetite=${apetite}`, { headers: this.headers });
  }

  operacional(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/api/indicadores/operacional`, { headers: this.headers });
  }

  planos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/api/acoes/planos`, { headers: this.headers });
  }

  integridade(): Observable<any> {
    return this.http.get(`${this.base}/api/trilha/integridade`, { headers: this.headers });
  }

  // --- Admin ---
  private adminHeaders(token: string): HttpHeaders {
    return new HttpHeaders({ 'X-Tenant-Id': this.tenant, 'X-Admin-Token': token });
  }

  adminLogin(login: string, senha: string): Observable<any> {
    return this.http.post(`${this.base}/api/admin/login`, { login, senha }, { headers: this.headers });
  }

  adminUsuarios(token: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/api/admin/usuarios`, { headers: this.adminHeaders(token) });
  }

  adminCriarUsuario(token: string, body: any): Observable<any> {
    return this.http.post(`${this.base}/api/admin/usuarios`, body, { headers: this.adminHeaders(token) });
  }

  adminAtivo(token: string, id: string, ativo: boolean): Observable<any> {
    return this.http.post(`${this.base}/api/admin/usuarios/${id}/ativo?ativo=${ativo}`, {}, { headers: this.adminHeaders(token) });
  }

  adminObservabilidade(token: string): Observable<any> {
    return this.http.get(`${this.base}/api/admin/observabilidade`, { headers: this.adminHeaders(token) });
  }

  // --- Catalogo / aplicabilidade ---
  normas(): Observable<any[]> { return this.http.get<any[]>(`${this.base}/api/catalogo/normas`, { headers: this.headers }); }
  requisito(norma: string, item: string, data?: string): Observable<any> {
    const q = `norma=${encodeURIComponent(norma)}&item=${encodeURIComponent(item)}${data ? '&data=' + encodeURIComponent(data) : ''}`;
    return this.http.get(`${this.base}/api/catalogo/requisito?${q}`, { headers: this.headers });
  }
  sugerir(cargo: string, local: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/api/catalogo/aplicabilidade?cargo=${cargo}&local=${local}`, { headers: this.headers });
  }

  // --- Governanca ---
  politicas(): Observable<any[]> { return this.http.get<any[]>(`${this.base}/api/governanca/politicas`, { headers: this.headers }); }
  criarPolitica(b: any): Observable<any> { return this.http.post(`${this.base}/api/governanca/politicas`, b, { headers: this.headers }); }
  publicarPolitica(cod: string): Observable<any> { return this.http.post(`${this.base}/api/governanca/politicas/${cod}/publicar`, { por: 'key-user' }, { headers: this.headers }); }
  darCiencia(cod: string, pseudonimo: string): Observable<any> { return this.http.post(`${this.base}/api/governanca/politicas/${cod}/ciencia`, { pseudonimo }, { headers: this.headers }); }
  pendenciasCiencia(cod: string): Observable<string[]> { return this.http.get<string[]>(`${this.base}/api/governanca/politicas/${cod}/pendencias`, { headers: this.headers }); }

  // --- Conferencia / OCR ---
  ocrTerceiro(empresa: string, documento: string, dias: number, duvidoso: boolean): Observable<any> {
    return this.http.post(`${this.base}/api/demo/ocr-terceiro?empresa=${empresa}&documento=${documento}&dias=${dias}&duvidoso=${duvidoso}`, {}, { headers: this.headers });
  }
  conferenciaPendentes(): Observable<any[]> { return this.http.get<any[]>(`${this.base}/api/conferencia/pendentes`, { headers: this.headers }); }
  conferir(id: string): Observable<any> { return this.http.post(`${this.base}/api/conferencia/${id}/conferir`, {}, { headers: this.headers }); }

  // --- Externo / dossie ---
  concederAcesso(b: any): Observable<any> { return this.http.post(`${this.base}/api/externo/acessos`, b, { headers: this.headers }); }
  gerarDossie(auditor: string, norma: string): Observable<any> { return this.http.post(`${this.base}/api/externo/dossie`, { auditor, norma }, { headers: this.headers }); }

  // --- MCP ---
  mcpEstado(): Observable<any> { return this.http.get(`${this.base}/api/mcp/estado`, { headers: this.headers }); }
  mcpLigar(): Observable<any> { return this.http.post(`${this.base}/api/mcp/ligar`, {}, { headers: this.headers }); }
  mcpDesligar(): Observable<any> { return this.http.post(`${this.base}/api/mcp/desligar`, {}, { headers: this.headers }); }
  mcpConsultarRisco(sujeito: string): Observable<any> { return this.http.post(`${this.base}/api/mcp/consultar-risco`, { sujeito }, { headers: this.headers }); }
  mcpBuscarRequisito(norma: string, item: string): Observable<any> { return this.http.post(`${this.base}/api/mcp/buscar-requisito`, { norma, item }, { headers: this.headers }); }
  mcpAbrirPlano(sujeito: string, causa: string, confirmado: boolean): Observable<any> { return this.http.post(`${this.base}/api/mcp/abrir-plano`, { sujeito, causa, confirmado }, { headers: this.headers }); }
  mcpAceitarRisco(): Observable<any> { return this.http.post(`${this.base}/api/mcp/aceitar-risco`, {}, { headers: this.headers }); }
}
