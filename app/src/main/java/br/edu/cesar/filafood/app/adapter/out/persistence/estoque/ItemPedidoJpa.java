package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Item do pedido de referência: produto e quantidade pedida. */
@Entity
@Table(name = "item_pedido")
public class ItemPedidoJpa {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpa pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoJpa produto;

    @Column(nullable = false)
    private int quantidade;

    protected ItemPedidoJpa() {
    }

    public ItemPedidoJpa(UUID id, PedidoJpa pedido, ProdutoJpa produto, int quantidade) {
        this.id = id;
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public UUID id() {
        return id;
    }

    public ProdutoJpa produto() {
        return produto;
    }

    public int quantidade() {
        return quantidade;
    }
}