package br.edu.cesar.filafood.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Pedido de referência enquanto a F4 não existe (ADR 0011): guarda só o que a baixa e a devolução
 * precisam — unidade, estado e itens.
 */
@Entity
@Table(name = "pedido")
public class PedidoJpa {

    @Id
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "endereco_latitude", precision = 9, scale = 6)
    private BigDecimal enderecoLatitude;

    @Column(name = "endereco_longitude", precision = 9, scale = 6)
    private BigDecimal enderecoLongitude;

    @Column(length = 255)
    private String endereco;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedidoJpa> itens = new ArrayList<>();

    protected PedidoJpa() {
    }

    public PedidoJpa(UUID id, UUID unidadeId, String estado) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.estado = estado;
    }

    public void mudarEstado(String estado) {
        this.estado = estado;
    }

    public UUID id() {
        return id;
    }

    public UUID unidadeId() {
        return unidadeId;
    }

    public String estado() {
        return estado;
    }

    public BigDecimal valor() {
        return valor;
    }

    public BigDecimal enderecoLatitude() {
        return enderecoLatitude;
    }

    public BigDecimal enderecoLongitude() {
        return enderecoLongitude;
    }

    public String endereco() {
        return endereco;
    }

    public List<ItemPedidoJpa> itens() {
        return itens;
    }
}