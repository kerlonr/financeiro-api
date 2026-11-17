import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';

export interface DadosConfirmacao {
  titulo: string;
  mensagem: string;
  acao: string;
}

/** Diálogo "tem certeza?" usado antes de excluir qualquer registro. Fecha com true ao confirmar. */
@Component({
  selector: 'app-confirmacao',
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './confirmacao.html',
})
export class Confirmacao {
  protected readonly dados = inject<DadosConfirmacao>(MAT_DIALOG_DATA);
}
