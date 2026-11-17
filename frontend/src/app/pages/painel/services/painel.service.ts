import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Painel } from '../model/painel.model';

@Injectable({ providedIn: 'root' })
export class PainelService {
  private readonly http = inject(HttpClient);

  resumo(): Observable<Painel> {
    return this.http.get<Painel>('/api/painel');
  }
}
