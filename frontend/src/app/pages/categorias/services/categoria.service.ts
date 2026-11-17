import { Injectable } from '@angular/core';
import { CrudApi } from '../../../common/crud-api';
import { Categoria, CategoriaRequest } from '../model/categoria.model';

@Injectable({ providedIn: 'root' })
export class CategoriaService extends CrudApi<Categoria, CategoriaRequest> {
  protected readonly url = '/api/categorias';
}
