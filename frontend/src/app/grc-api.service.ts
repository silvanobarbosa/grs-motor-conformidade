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
}
