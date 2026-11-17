export interface Conta {
  id: number;
  nome: string;
  saldoInicial: number;
  /** Saldo inicial + entradas - saídas, calculado pela API. */
  saldo: number;
}

export interface ContaRequest {
  nome: string;
  saldoInicial: number;
}
