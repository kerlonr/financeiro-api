import { ComponentType } from '@angular/cdk/portal';
import { inject, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, EMPTY, filter, Observable, switchMap, tap } from 'rxjs';
import { Confirmacao, DadosConfirmacao } from './confirmacao/confirmacao';
import { mensagemDeErro } from './erro-api';

/** Interações repetidas em todas as telas de cadastro: abrir formulário, excluir e avisar o usuário. */
@Injectable({ providedIn: 'root' })
export class DialogosService {
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  /** Abre um formulário em diálogo. Emite apenas se o usuário salvou. */
  abrirFormulario<D>(formulario: ComponentType<unknown>, dados: D | null = null): Observable<true> {
    return this.dialog
      .open(formulario, { data: dados, autoFocus: 'first-tabbable', width: '480px' })
      .afterClosed()
      .pipe(
        filter((salvou): salvou is true => salvou === true),
        tap(() => this.avisar('Salvo.')),
      );
  }

  /** Pede confirmação, executa a exclusão e avisa o resultado. Emite apenas se excluiu. */
  confirmarExclusao(
    confirmacao: DadosConfirmacao,
    exclusao: () => Observable<void>,
  ): Observable<void> {
    return this.dialog
      .open(Confirmacao, { data: confirmacao, width: '400px' })
      .afterClosed()
      .pipe(
        filter((confirmou) => confirmou === true),
        switchMap(() => exclusao()),
        tap(() => this.avisar('Excluído.')),
        catchError((erro) => {
          this.avisar(mensagemDeErro(erro));
          return EMPTY;
        }),
      );
  }

  private avisar(mensagem: string): void {
    this.snackBar.open(mensagem, 'Fechar', { duration: 4000 });
  }
}
