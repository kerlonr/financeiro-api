import { CurrencyPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { Transacao } from '../model/transacao.model';

/**
 * Uma transação em formato de linha: ícone, descrição, detalhe e valor com sinal.
 * As ações (menu) entram por projeção de conteúdo, porque mudam conforme a tela.
 */
@Component({
  selector: 'app-linha-transacao',
  imports: [CurrencyPipe, MatIconModule],
  templateUrl: './linha-transacao.html',
  styleUrl: './linha-transacao.css',
})
export class LinhaTransacao {
  readonly transacao = input.required<Transacao>();

  protected readonly ehEntrada = computed(() => this.transacao().tipo === 'ENTRADA');
  protected readonly ehTransferencia = computed(() => !!this.transacao().transferenciaId);
  protected readonly icone = computed(() => {
    if (this.ehTransferencia()) {
      return 'sync_alt';
    }
    return this.ehEntrada() ? 'south_west' : 'north_east';
  });
}
