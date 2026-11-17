package br.edu.setrem.financeiro.categoria;

import br.edu.setrem.financeiro.common.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Categoria extends EntidadeBase {

    @Column(name = "usuario_id")
    public Long usuarioId;

    public String nome;
}
