import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { deDataIso, paraDataIso } from '../../../common/datas';
import { EnvioFormulario } from '../../../common/envio-formulario';
import { CategoriaService } from '../../categorias/services/categoria.service';
import { ContaService } from '../../contas/services/conta.service';
import { TipoTransacao, Transacao, TransacaoRequest } from '../model/transacao.model';
import { TransacaoService } from '../services/transacao.service';

/** Diálogo para lançar (sem dados) ou editar (com a transação) uma entrada ou saída. */
@Component({
  selector: 'app-transacao-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonToggleModule,
    MatButtonModule,
  ],
  templateUrl: './transacao-form.html',
  styleUrl: './transacao-form.css',
})
export class TransacaoForm {
  private readonly transacaoService = inject(TransacaoService);
  private readonly dialogRef = inject(MatDialogRef<TransacaoForm>);

  protected readonly transacao = inject<Transacao | null>(MAT_DIALOG_DATA);
  protected readonly contas = toSignal(inject(ContaService).listar(), { initialValue: [] });
  protected readonly categorias = toSignal(inject(CategoriaService).listar(), { initialValue: [] });
  protected readonly envio = new EnvioFormulario();
  protected readonly form = inject(NonNullableFormBuilder).group({
    tipo: [this.transacao?.tipo ?? ('SAIDA' as TipoTransacao), Validators.required],
    valor: [
      this.transacao?.valor ?? (null as number | null),
      [Validators.required, Validators.min(0.01)],
    ],
    descricao: [this.transacao?.descricao ?? '', [Validators.required, Validators.maxLength(120)]],
    data: [this.transacao ? deDataIso(this.transacao.data) : new Date(), Validators.required],
    contaId: [this.transacao?.contaId ?? (null as number | null), Validators.required],
    categoriaId: [this.transacao?.categoriaId ?? (null as number | null), Validators.required],
  });

  salvar(): void {
    const valores = this.form.getRawValue();
    const dados: TransacaoRequest = {
      tipo: valores.tipo,
      valor: valores.valor ?? 0,
      descricao: valores.descricao.trim(),
      data: paraDataIso(valores.data),
      contaId: valores.contaId ?? 0,
      categoriaId: valores.categoriaId ?? 0,
    };
    const requisicao = () =>
      this.transacao
        ? this.transacaoService.atualizar(this.transacao.id, dados)
        : this.transacaoService.criar(dados);

    this.envio.enviar(this.form, requisicao, () => this.dialogRef.close(true));
  }
}
