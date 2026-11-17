import { Injectable } from '@angular/core';
import { CrudApi } from '../../../common/crud-api';
import { Conta, ContaRequest } from '../model/conta.model';

@Injectable({ providedIn: 'root' })
export class ContaService extends CrudApi<Conta, ContaRequest> {
  protected readonly url = '/api/contas';
}
