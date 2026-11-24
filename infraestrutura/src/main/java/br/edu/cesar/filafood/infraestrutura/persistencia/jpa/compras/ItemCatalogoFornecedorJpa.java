package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.compras;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_catalogo_fornecedor")
public class ItemCatalogoFornecedorJpa {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private FornecedorJpa fornecedor;

    @Column(name = "insumo_id", nullable = false)
    private UUID insumoId;

    @Column(name = "preco_unitario", precision = 12, scale = 2, nullable = false)
    private BigDecimal precoUnitario;

    @Column(name = "quantidade_minima", precision = 12, scale = 3, nullable = false)
    private BigDecimal quantidadeMinima;

    @Column(name = "prazo_de_entrega_em_dias", nullable = false)
    private int prazoDeEntregaEmDias;

    protected ItemCatalogoFornecedorJpa() {
    }

    public ItemCatalogoFornecedorJpa(UUID id, FornecedorJpa fornecedor, UUID insumoId, BigDecimal precoUnitario,
            BigDecimal quantidadeMinima, int prazoDeEntregaEmDias) {
        this.id = id;
        this.fornecedor = fornecedor;
        this.insumoId = insumoId;
        this.precoUnitario = precoUnitario;
        this.quantidadeMinima = quantidadeMinima;
        this.prazoDeEntregaEmDias = prazoDeEntregaEmDias;
    }

    void atualizar(BigDecimal precoUnitario, BigDecimal quantidadeMinima, int prazoDeEntregaEmDias) {
        this.precoUnitario = precoUnitario;
        this.quantidadeMinima = quantidadeMinima;
        this.prazoDeEntregaEmDias = prazoDeEntregaEmDias;
    }

    public UUID getId() { return id; }
    public UUID getInsumoId() { return insumoId; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public BigDecimal getQuantidadeMinima() { return quantidadeMinima; }
    public int getPrazoDeEntregaEmDias() { return prazoDeEntregaEmDias; }
}
