import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Quando a API responde 401 (access token de 15 min venceu), renova a sessão uma vez
 * e repete a requisição original. Se a renovação falhar, o usuário volta para o login.
 */
export const sessaoInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const authService = inject(AuthService);
  // Login, cadastro, refresh e logout respondem 401 por motivos próprios: não tentam renovar.
  const rotaDeEntrada =
    requisicao.url.startsWith('/api/auth/') && requisicao.url !== '/api/auth/me';

  return proximo(requisicao).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status !== 401 || rotaDeEntrada) {
        return throwError(() => erro);
      }
      return authService.renovarSessao().pipe(
        catchError(() => {
          authService.encerrarLocalmente();
          return throwError(() => erro);
        }),
        switchMap(() => proximo(requisicao)),
      );
    }),
  );
};
