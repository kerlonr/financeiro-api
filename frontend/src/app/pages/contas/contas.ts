import { CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { DialogosService } from '../../common/dialogos.service';
import { ContaForm } from './conta-form/conta-form';
import { Conta } from './model/conta.model';
import { ContaService } from './services/conta.service';

@Component({
  selector: 'app-contas',
  imports: [CurrencyPipe, MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './contas.html',
})
export class Contas {
  private readonly contaService = inject(ContaService);
  private readonly dialogos = inject(DialogosService);

  protected readonly contas = rxResource({
    stream: () => this.contaService.listar(),
    defaultValue: [],
  });

  abrirFormulario(conta: Conta | null = null): void {
    this.dialogos.abrirFormulario(ContaForm, conta).subscribe(() => this.contas.reload());
  }

  excluir(conta: Conta): void {
    this.dialogos
      .confirmarExclusao(
        {
          titulo: 'Excluir conta?',
          mensagem: `A conta "${conta.nome}" será removida. Contas com lançamentos não podem ser excluídas.`,
          acao: 'Excluir',
        },
        () => this.contaService.excluir(conta.id),
      )
      .subscribe(() => this.contas.reload());
  }
}
