import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { DialogosService } from '../../common/dialogos.service';
import { CategoriaForm } from './categoria-form/categoria-form';
import { Categoria } from './model/categoria.model';
import { CategoriaService } from './services/categoria.service';

@Component({
  selector: 'app-categorias',
  imports: [MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './categorias.html',
})
export class Categorias {
  private readonly categoriaService = inject(CategoriaService);
  private readonly dialogos = inject(DialogosService);

  protected readonly categorias = rxResource({
    stream: () => this.categoriaService.listar(),
    defaultValue: [],
  });

  abrirFormulario(categoria: Categoria | null = null): void {
    this.dialogos
      .abrirFormulario(CategoriaForm, categoria)
      .subscribe(() => this.categorias.reload());
  }

  excluir(categoria: Categoria): void {
    this.dialogos
      .confirmarExclusao(
        {
          titulo: 'Excluir categoria?',
          mensagem: `A categoria "${categoria.nome}" será removida. Categorias com lançamentos não podem ser excluídas.`,
          acao: 'Excluir',
        },
        () => this.categoriaService.excluir(categoria.id),
      )
      .subscribe(() => this.categorias.reload());
  }
}
