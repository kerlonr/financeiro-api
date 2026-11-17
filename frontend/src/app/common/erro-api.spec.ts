import { HttpErrorResponse } from '@angular/common/http';
import { mensagemDeErro } from './erro-api';

function erroHttp(status: number, corpo: unknown = null): HttpErrorResponse {
  return new HttpErrorResponse({ status, error: corpo });
}

describe('mensagemDeErro', () => {
  it('usa a mensagem enviada pela API', () => {
    expect(mensagemDeErro(erroHttp(422, { mensagem: 'Já existe uma conta com este nome.' }))).toBe(
      'Já existe uma conta com este nome.',
    );
  });

  it('prefere o erro do campo quando a validação falha', () => {
    const corpo = { mensagem: 'Dados inválidos.', campos: { valor: 'Valor inválido.' } };
    expect(mensagemDeErro(erroHttp(400, corpo))).toBe('Valor inválido.');
  });

  it('explica falta de conexão e excesso de tentativas', () => {
    expect(mensagemDeErro(erroHttp(0))).toBe('Sem conexão com o servidor.');
    expect(mensagemDeErro(erroHttp(429))).toContain('Muitas tentativas');
  });

  it('tem uma mensagem genérica para o resto', () => {
    expect(mensagemDeErro(erroHttp(500))).toBe('Algo deu errado. Tente novamente.');
    expect(mensagemDeErro(new Error('x'))).toBe('Algo deu errado. Tente novamente.');
  });
});
