import { HttpErrorResponse } from '@angular/common/http';

interface ErroResponse {
  mensagem?: string;
  campos?: Record<string, string>;
}

/** Texto amigável a partir de um erro da API (formato ErroResponse do backend). */
export function mensagemDeErro(erro: unknown): string {
  if (!(erro instanceof HttpErrorResponse)) {
    return 'Algo deu errado. Tente novamente.';
  }
  if (erro.status === 0) {
    return 'Sem conexão com o servidor.';
  }
  if (erro.status === 429) {
    return 'Muitas tentativas seguidas. Aguarde um minuto.';
  }

  const corpo = erro.error as ErroResponse | null;
  const primeiroCampo = corpo?.campos ? Object.values(corpo.campos)[0] : undefined;
  return primeiroCampo ?? corpo?.mensagem ?? 'Algo deu errado. Tente novamente.';
}
