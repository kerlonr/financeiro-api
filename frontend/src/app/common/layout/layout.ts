import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../pages/auth/services/auth.service';

/** Moldura das páginas internas: menu lateral (ou inferior no celular) e área de conteúdo. */
@Component({
  selector: 'app-layout',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class Layout {
  protected readonly authService = inject(AuthService);

  protected readonly menu = [
    { rota: '/', icone: 'space_dashboard', titulo: 'Início' },
    { rota: '/transacoes', icone: 'receipt_long', titulo: 'Transações' },
    { rota: '/contas', icone: 'account_balance', titulo: 'Contas' },
    { rota: '/categorias', icone: 'sell', titulo: 'Categorias' },
  ];
}
