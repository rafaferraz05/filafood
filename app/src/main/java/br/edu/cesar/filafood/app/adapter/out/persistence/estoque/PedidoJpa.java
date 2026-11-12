package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

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

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedidoJpa> itens = new ArrayList<>();

    protected PedidoJpa() {
    }

    public PedidoJpa(UUID id, UUID unidadeId, String estado) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.estado = estado;
    }

    void mudarEstado(String estado) {
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

    public List<ItemPedidoJpa> itens() {
        return itens;
    }
}