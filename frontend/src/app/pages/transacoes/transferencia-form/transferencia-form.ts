import { CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { paraDataIso } from '../../../common/datas';
import { EnvioFormulario } from '../../../common/envio-formulario';
import { ContaService } from '../../contas/services/conta.service';
import { TransacaoService } from '../services/transacao.service';

/** Diálogo de transferência entre duas contas do usuário. */
@Component({
  selector: 'app-transferencia-form',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
  ],
  templateUrl: './transferencia-form.html',
})
export class TransferenciaForm {
  private readonly transacaoService = inject(TransacaoService);
  private readonly dialogRef = inject(MatDialogRef<TransferenciaForm>);

  protected readonly contas = toSignal(inject(ContaService).listar(), { initialValue: [] });
  protected readonly envio = new EnvioFormulario();
  protected readonly form = inject(NonNullableFormBuilder).group({
    contaOrigemId: [null as number | null, Validators.required],
    contaDestinoId: [null as number | null, Validators.required],
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
    data: [new Date(), Validators.required],
    descricao: ['', Validators.maxLength(120)],
  });

  salvar(): void {
    const valores = this.form.getRawValue();
    const dados = {
      contaOrigemId: valores.contaOrigemId ?? 0,
      contaDestinoId: valores.contaDestinoId ?? 0,
      valor: valores.valor ?? 0,
      data: paraDataIso(valores.data),
      descricao: valores.descricao.trim(),
    };

    this.envio.enviar(
      this.form,
      () => this.transacaoService.transferir(dados),
      () => this.dialogRef.close(true),
    );
  }
}
