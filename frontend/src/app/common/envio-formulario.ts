import { signal } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { finalize, Observable } from 'rxjs';
import { mensagemDeErro } from './erro-api';

/**
 * Fluxo de envio usado por todos os formulários do app:
 * valida, bloqueia o botão enquanto envia e mostra a mensagem de erro da API.
 */
export class EnvioFormulario {
  readonly enviando = signal(false);
  readonly erro = signal<string | null>(null);

  enviar<T>(
    form: AbstractControl,
    requisicao: () => Observable<T>,
    aoConcluir: (resposta: T) => void,
  ): void {
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.erro.set(null);
    requisicao()
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: aoConcluir,
        error: (erro) => this.erro.set(mensagemDeErro(erro)),
      });
  }
}
