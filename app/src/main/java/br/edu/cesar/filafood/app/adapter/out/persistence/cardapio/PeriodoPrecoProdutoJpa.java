package br.edu.cesar.filafood.app.adapter.out.persistence.cardapio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "periodo_preco_produto")
public class PeriodoPrecoProdutoJpa {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoCardapioJpa produto;

    @Column(name = "preco", precision = 12, scale = 2, nullable = false)
    private BigDecimal preco;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    protected PeriodoPrecoProdutoJpa() {
    }

    public PeriodoPrecoProdutoJpa(UUID id, ProdutoCardapioJpa produto, BigDecimal preco,
            LocalDate dataInicio, LocalDate dataFim) {
        this.id = id;
        this.produto = produto;
        this.preco = preco;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public UUID getId() { return id; }
    public BigDecimal getPreco() { return preco; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
}