import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Páginas internas: só com usuário logado. */
export const logadoGuard: CanActivateFn = () =>
  inject(AuthService).usuario() !== null || inject(Router).createUrlTree(['/login']);

/** Login e cadastro: quem já está logado vai direto para o painel. */
export const visitanteGuard: CanActivateFn = () =>
  inject(AuthService).usuario() === null || inject(Router).createUrlTree(['/']);
