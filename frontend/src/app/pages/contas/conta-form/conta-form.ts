import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { EnvioFormulario } from '../../../common/envio-formulario';
import { Conta } from '../model/conta.model';
import { ContaService } from '../services/conta.service';

/** Diálogo para criar (sem dados) ou editar (com a conta) uma conta. Fecha com true ao salvar. */
@Component({
  selector: 'app-conta-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './conta-form.html',
})
export class ContaForm {
  private readonly contaService = inject(ContaService);
  private readonly dialogRef = inject(MatDialogRef<ContaForm>);

  protected readonly conta = inject<Conta | null>(MAT_DIALOG_DATA);
  protected readonly envio = new EnvioFormulario();
  protected readonly form = inject(NonNullableFormBuilder).group({
    nome: [this.conta?.nome ?? '', [Validators.required, Validators.maxLength(60)]],
    saldoInicial: [this.conta?.saldoInicial ?? 0, Validators.required],
  });

  salvar(): void {
    const dados = { ...this.form.getRawValue(), nome: this.form.controls.nome.value.trim() };
    const requisicao = () =>
      this.conta
        ? this.contaService.atualizar(this.conta.id, dados)
        : this.contaService.criar(dados);

    this.envio.enviar(this.form, requisicao, () => this.dialogRef.close(true));
  }
}
