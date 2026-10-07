package entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Transacao extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String descricao;
    public BigDecimal valor;
    public String tipo;
    public LocalDate datatransacao;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    public Conta conta;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    public Categoria categoria;
}
