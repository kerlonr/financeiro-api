export type TipoTransacao = 'ENTRADA' | 'SAIDA';

export interface Transacao {
  id: number;
  tipo: TipoTransacao;
  valor: number;
  descricao: string;
  /** "AAAA-MM-DD" */
  data: string;
  contaId: number;
  contaNome: string;
  /** Ausente nas transações geradas por transferência. */
  categoriaId?: number;
  categoriaNome?: string;
  /** Presente apenas nas transações geradas por transferência. */
  transferenciaId?: string;
}

export interface TransacaoRequest {
  contaId: number;
  categoriaId: number;
  tipo: TipoTransacao;
  valor: number;
  descricao: string;
  data: string;
}

export interface TransferenciaRequest {
  contaOrigemId: number;
  contaDestinoId: number;
  valor: number;
  data: string;
  descricao: string;
}

export interface FiltroTransacoes {
  contaId: number | null;
  categoriaId: number | null;
  inicio: string;
  fim: string;
}
