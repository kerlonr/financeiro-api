import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, finalize, Observable, of, shareReplay, tap } from 'rxjs';
import { CadastroRequest, LoginRequest, Usuario } from '../model/usuario.model';

/**
 * Estado da sessão no navegador. Os tokens ficam em cookies httpOnly que o JavaScript
 * não consegue ler: aqui guardamos apenas quem é o usuário logado.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private renovacaoEmAndamento: Observable<void> | null = null;

  readonly usuario = signal<Usuario | null>(null);

  /** Chamado ao abrir o app: descobre se ainda existe uma sessão válida. */
  carregarUsuario(): Observable<Usuario | null> {
    return this.http.get<Usuario>('/api/auth/me').pipe(
      tap((usuario) => this.usuario.set(usuario)),
      catchError(() => of(null)),
    );
  }

  entrar(dados: LoginRequest): Observable<Usuario> {
    return this.http
      .post<Usuario>('/api/auth/login', dados)
      .pipe(tap((usuario) => this.usuario.set(usuario)));
  }

  cadastrar(dados: CadastroRequest): Observable<Usuario> {
    return this.http
      .post<Usuario>('/api/auth/cadastro', dados)
      .pipe(tap((usuario) => this.usuario.set(usuario)));
  }

  sair(): void {
    this.http
      .post<void>('/api/auth/logout', null)
      .pipe(finalize(() => this.encerrarLocalmente()))
      .subscribe({ error: () => undefined });
  }

  /**
   * Troca o refresh token por um novo par de tokens.
   * Várias requisições podem receber 401 ao mesmo tempo; todas aguardam a mesma renovação,
   * porque reutilizar um refresh token já trocado derruba a sessão (proteção contra roubo).
   */
  renovarSessao(): Observable<void> {
    this.renovacaoEmAndamento ??= this.http.post<void>('/api/auth/refresh', null).pipe(
      finalize(() => (this.renovacaoEmAndamento = null)),
      shareReplay(1),
    );
    return this.renovacaoEmAndamento;
  }

  /** Sessão acabou: limpa o estado e, se havia alguém logado, volta para o login. */
  encerrarLocalmente(): void {
    const estavaLogado = this.usuario() !== null;
    this.usuario.set(null);
    if (estavaLogado) {
      this.router.navigate(['/login']);
    }
  }
}
