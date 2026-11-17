import { Conta } from '../../contas/model/conta.model';
import { Transacao } from '../../transacoes/model/transacao.model';

export interface Painel {
  saldoTotal: number;
  contas: Conta[];
  entradasDoMes: number;
  saidasDoMes: number;
  ultimasTransacoes: Transacao[];
}
