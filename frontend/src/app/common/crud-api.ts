import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * Chamadas REST padrão de um cadastro (listar, criar, atualizar, excluir).
 * Cada service de funcionalidade só informa a URL do recurso.
 */
export abstract class CrudApi<Item, Dados> {
  protected readonly http = inject(HttpClient);
  protected abstract readonly url: string;

  listar(): Observable<Item[]> {
    return this.http.get<Item[]>(this.url);
  }

  criar(dados: Dados): Observable<void> {
    return this.http.post<void>(this.url, dados);
  }

  atualizar(id: number, dados: Dados): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
