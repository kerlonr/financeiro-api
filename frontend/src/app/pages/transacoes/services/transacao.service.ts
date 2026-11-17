import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudApi } from '../../../common/crud-api';
import {
  FiltroTransacoes,
  Transacao,
  TransacaoRequest,
  TransferenciaRequest,
} from '../model/transacao.model';

@Injectable({ providedIn: 'root' })
export class TransacaoService extends CrudApi<Transacao, TransacaoRequest> {
  protected readonly url = '/api/transacoes';

  filtrar(filtro: FiltroTransacoes): Observable<Transacao[]> {
    let parametros = new HttpParams().set('inicio', filtro.inicio).set('fim', filtro.fim);
    if (filtro.contaId !== null) {
      parametros = parametros.set('contaId', filtro.contaId);
    }
    if (filtro.categoriaId !== null) {
      parametros = parametros.set('categoriaId', filtro.categoriaId);
    }
    return this.http.get<Transacao[]>(this.url, { params: parametros });
  }

  transferir(dados: TransferenciaRequest): Observable<void> {
    return this.http.post<void>(`${this.url}/transferencia`, dados);
  }
}
