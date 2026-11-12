package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.app.application.estoque.PedidoDoPreparo;
import br.edu.cesar.filafood.app.application.estoque.PedidoParaPreparo;
import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.consumo.ItemDoPedido;

/** Adaptador de referência da porta {@link PedidoDoPreparo} (ADR 0011). */
@Component
class PedidoJpaAdapter implements PedidoDoPreparo {

    private final PedidoJpaRepository pedidos;

    PedidoJpaAdapter(PedidoJpaRepository pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public PedidoParaPreparo porId(String pedidoId) {
        PedidoJpa pedido = pedidos.findById(UUID.fromString(pedidoId))
                .orElseThrow(() -> new RegraDeNegocioException("pedido não encontrado"));
        List<ItemDoPedido> itens = pedido.itens().stream()
                .map(item -> new ItemDoPedido(item.produto().id().toString(), item.quantidade()))
                .toList();
        return new PedidoParaPreparo(pedido.id().toString(), pedido.unidadeId().toString(),
                EstadoDoPedido.valueOf(pedido.estado()), itens);
    }

    @Override
    public void marcarEmPreparo(String pedidoId) {
        PedidoJpa pedido = pedidos.findById(UUID.fromString(pedidoId))
                .orElseThrow(() -> new RegraDeNegocioException("pedido não encontrado"));
        pedido.mudarEstado(EstadoDoPedido.EM_PREPARO.name());
        pedidos.save(pedido);
    }
}