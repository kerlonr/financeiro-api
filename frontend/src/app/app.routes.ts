import { Routes } from '@angular/router';
import { Layout } from './common/layout/layout';
import { logadoGuard, visitanteGuard } from './pages/auth/services/auth.guards';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Entrar · Lastro',
    canActivate: [visitanteGuard],
    loadComponent: () => import('./pages/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'cadastro',
    title: 'Criar conta · Lastro',
    canActivate: [visitanteGuard],
    loadComponent: () => import('./pages/auth/cadastro/cadastro').then((m) => m.Cadastro),
  },
  {
    path: '',
    component: Layout,
    canActivate: [logadoGuard],
    children: [
      {
        path: '',
        title: 'Início · Lastro',
        loadComponent: () => import('./pages/painel/painel').then((m) => m.Painel),
      },
      {
        path: 'transacoes',
        title: 'Transações · Lastro',
        loadComponent: () => import('./pages/transacoes/transacoes').then((m) => m.Transacoes),
      },
      {
        path: 'contas',
        title: 'Contas · Lastro',
        loadComponent: () => import('./pages/contas/contas').then((m) => m.Contas),
      },
      {
        path: 'categorias',
        title: 'Categorias · Lastro',
        loadComponent: () => import('./pages/categorias/categorias').then((m) => m.Categorias),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
