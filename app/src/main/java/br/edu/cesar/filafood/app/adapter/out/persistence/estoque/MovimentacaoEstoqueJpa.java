package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entidade JPA da movimentação de estoque: linha imutável, ligada ao insumo. */
@Entity
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoqueJpa {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private InsumoJpa insumo;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String motivo;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoUnitario;

    @Column(nullable = false, length = 80)
    private String origem;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected MovimentacaoEstoqueJpa() {
    }

    public MovimentacaoEstoqueJpa(UUID id, InsumoJpa insumo, String tipo, String motivo, BigDecimal quantidade,
            BigDecimal custoUnitario, String origem, Instant dataHora) {
        this.id = id;
        this.insumo = insumo;
        this.tipo = tipo;
        this.motivo = motivo;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.origem = origem;
        this.dataHora = dataHora;
    }

    public UUID id() {
        return id;
    }

    public InsumoJpa insumo() {
        return insumo;
    }

    public String tipo() {
        return tipo;
    }

    public String motivo() {
        return motivo;
    }

    public BigDecimal quantidade() {
        return quantidade;
    }

    public BigDecimal custoUnitario() {
        return custoUnitario;
    }

    public String origem() {
        return origem;
    }

    public Instant dataHora() {
        return dataHora;
    }
}
