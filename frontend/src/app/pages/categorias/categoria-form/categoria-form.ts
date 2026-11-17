import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { EnvioFormulario } from '../../../common/envio-formulario';
import { Categoria } from '../model/categoria.model';
import { CategoriaService } from '../services/categoria.service';

/** Diálogo para criar (sem dados) ou editar (com a categoria) uma categoria. Fecha com true ao salvar. */
@Component({
  selector: 'app-categoria-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './categoria-form.html',
})
export class CategoriaForm {
  private readonly categoriaService = inject(CategoriaService);
  private readonly dialogRef = inject(MatDialogRef<CategoriaForm>);

  protected readonly categoria = inject<Categoria | null>(MAT_DIALOG_DATA);
  protected readonly envio = new EnvioFormulario();
  protected readonly form = inject(NonNullableFormBuilder).group({
    nome: [this.categoria?.nome ?? '', [Validators.required, Validators.maxLength(60)]],
  });

  salvar(): void {
    const dados = { nome: this.form.controls.nome.value.trim() };
    const requisicao = () =>
      this.categoria
        ? this.categoriaService.atualizar(this.categoria.id, dados)
        : this.categoriaService.criar(dados);

    this.envio.enviar(this.form, requisicao, () => this.dialogRef.close(true));
  }
}
