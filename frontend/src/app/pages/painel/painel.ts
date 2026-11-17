import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { DialogosService } from '../../common/dialogos.service';
import { AuthService } from '../auth/services/auth.service';
import { LinhaTransacao } from '../transacoes/linha-transacao/linha-transacao';
import { TransacaoForm } from '../transacoes/transacao-form/transacao-form';
import { TransferenciaForm } from '../transacoes/transferencia-form/transferencia-form';
import { PainelService } from './services/painel.service';

@Component({
  selector: 'app-painel',
  imports: [CurrencyPipe, DatePipe, RouterLink, MatButtonModule, MatIconModule, LinhaTransacao],
  templateUrl: './painel.html',
  styleUrl: './painel.css',
})
export class Painel {
  private readonly painelService = inject(PainelService);
  private readonly dialogos = inject(DialogosService);
  private readonly authService = inject(AuthService);

  protected readonly hoje = new Date();
  protected readonly primeiroNome = computed(() => this.authService.usuario()?.nome.split(' ')[0]);
  protected readonly painel = rxResource({ stream: () => this.painelService.resumo() });

  novaTransacao(): void {
    this.dialogos.abrirFormulario(TransacaoForm).subscribe(() => this.painel.reload());
  }

  transferir(): void {
    this.dialogos.abrirFormulario(TransferenciaForm).subscribe(() => this.painel.reload());
  }
}
