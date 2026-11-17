import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource, toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { deDataIso, paraDataIso } from '../../common/datas';
import { DialogosService } from '../../common/dialogos.service';
import { CategoriaService } from '../categorias/services/categoria.service';
import { ContaService } from '../contas/services/conta.service';
import { LinhaTransacao } from './linha-transacao/linha-transacao';
import { TipoTransacao, Transacao } from './model/transacao.model';
import { TransacaoService } from './services/transacao.service';
import { TransacaoForm } from './transacao-form/transacao-form';
import { TransferenciaForm } from './transferencia-form/transferencia-form';

interface DiaDeTransacoes {
  dia: Date;
  transacoes: Transacao[];
}

@Component({
  selector: 'app-transacoes',
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatMenuModule,
    MatSelectModule,
    LinhaTransacao,
  ],
  templateUrl: './transacoes.html',
  styleUrl: './transacoes.css',
})
export class Transacoes {
  private readonly transacaoService = inject(TransacaoService);
  private readonly dialogos = inject(DialogosService);

  protected readonly contas = toSignal(inject(ContaService).listar(), { initialValue: [] });
  protected readonly categorias = toSignal(inject(CategoriaService).listar(), { initialValue: [] });

  /** Filtros: primeiro dia do mês exibido, conta e categoria (null = todas). */
  protected readonly mes = signal(primeiroDiaDoMes(new Date()));
  protected readonly contaId = signal<number | null>(null);
  protected readonly categoriaId = signal<number | null>(null);

  /** Recarrega sozinho sempre que algum filtro muda. */
  protected readonly transacoes = rxResource({
    params: () => ({
      contaId: this.contaId(),
      categoriaId: this.categoriaId(),
      inicio: paraDataIso(this.mes()),
      fim: paraDataIso(new Date(this.mes().getFullYear(), this.mes().getMonth() + 1, 0)),
    }),
    stream: ({ params }) => this.transacaoService.filtrar(params),
    defaultValue: [],
  });

  // value() lança erro se a requisição falhou; nesse caso a lista é tratada como vazia.
  private readonly lista = computed(() =>
    this.transacoes.hasValue() ? this.transacoes.value() : [],
  );
  protected readonly dias = computed(() => agruparPorDia(this.lista()));

  // Transferências não entram nos totais: o dinheiro só mudou de conta.
  protected readonly entradas = computed(() => this.somar('ENTRADA'));
  protected readonly saidas = computed(() => this.somar('SAIDA'));

  mudarMes(deslocamento: number): void {
    const atual = this.mes();
    this.mes.set(new Date(atual.getFullYear(), atual.getMonth() + deslocamento, 1));
  }

  novaTransacao(): void {
    this.dialogos.abrirFormulario(TransacaoForm).subscribe(() => this.transacoes.reload());
  }

  transferir(): void {
    this.dialogos.abrirFormulario(TransferenciaForm).subscribe(() => this.transacoes.reload());
  }

  editar(transacao: Transacao): void {
    this.dialogos
      .abrirFormulario(TransacaoForm, transacao)
      .subscribe(() => this.transacoes.reload());
  }

  excluir(transacao: Transacao): void {
    const mensagem = transacao.transferenciaId
      ? 'Os dois lados da transferência (saída e entrada) serão excluídos.'
      : `"${transacao.descricao}" será excluída.`;

    this.dialogos
      .confirmarExclusao({ titulo: 'Excluir transação?', mensagem, acao: 'Excluir' }, () =>
        this.transacaoService.excluir(transacao.id),
      )
      .subscribe(() => this.transacoes.reload());
  }

  private somar(tipo: TipoTransacao): number {
    // Soma em centavos inteiros para evitar erros de arredondamento de ponto flutuante.
    const centavos = this.lista()
      .filter((transacao) => transacao.tipo === tipo && !transacao.transferenciaId)
      .reduce((total, transacao) => total + Math.round(transacao.valor * 100), 0);
    return centavos / 100;
  }
}

function primeiroDiaDoMes(data: Date): Date {
  return new Date(data.getFullYear(), data.getMonth(), 1);
}

/** A API já devolve ordenado da mais recente para a mais antiga. */
function agruparPorDia(transacoes: Transacao[]): DiaDeTransacoes[] {
  const dias: DiaDeTransacoes[] = [];
  for (const transacao of transacoes) {
    const ultimo = dias.at(-1);
    if (ultimo && paraDataIso(ultimo.dia) === transacao.data) {
      ultimo.transacoes.push(transacao);
    } else {
      dias.push({ dia: deDataIso(transacao.data), transacoes: [transacao] });
    }
  }
  return dias;
}
