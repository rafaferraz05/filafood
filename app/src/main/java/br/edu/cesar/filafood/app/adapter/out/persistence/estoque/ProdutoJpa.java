package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Produto do cardápio, mantido aqui apenas como referência da ficha técnica enquanto a F6 não tem
 * tabela própria (ADR 0011).
 */
@Entity
@Table(name = "produto")
public class ProdutoJpa {

    @Id
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false, length = 20)
    private String estado;

    protected ProdutoJpa() {
    }

    public ProdutoJpa(UUID id, UUID unidadeId, String nome, BigDecimal preco, String estado) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.nome = nome;
        this.preco = preco;
        this.estado = estado;
    }

    public UUID id() {
        return id;
    }

    public UUID unidadeId() {
        return unidadeId;
    }

    public String nome() {
        return nome;
    }

    public BigDecimal preco() {
        return preco;
    }

    public String estado() {
        return estado;
    }
}
