package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.aplicacao.entrega.pedido.PedidoDaEntrega;
import br.edu.cesar.filafood.aplicacao.entrega.pedido.PedidoDaEntregaRepositorioAplicacao;
import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;
import br.edu.cesar.filafood.infraestrutura.persistencia.jpa.PedidoJpa;
import br.edu.cesar.filafood.infraestrutura.persistencia.jpa.PedidoJpaRepository;

/** Adaptador de referência da porta {@link PedidoDaEntregaRepositorioAplicacao} (ADR 0011). */
@Component
class PedidoDaEntregaRepositorioJpa implements PedidoDaEntregaRepositorioAplicacao {

    private final PedidoJpaRepository pedidos;

    PedidoDaEntregaRepositorioJpa(PedidoJpaRepository pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public PedidoDaEntrega porId(String pedidoId) {
        PedidoJpa pedido = pedido(pedidoId);
        if (pedido.enderecoLatitude() == null || pedido.enderecoLongitude() == null) {
            throw new RegraDeNegocioException("pedido sem endereço de entrega");
        }
        return new PedidoDaEntrega(pedido.id().toString(), pedido.unidadeId().toString(),
                EstadoDoPedido.valueOf(pedido.estado()), pedido.valor(),
                new Coordenada(pedido.enderecoLatitude().doubleValue(), pedido.enderecoLongitude().doubleValue()),
                pedido.endereco());
    }

    @Override
    public void marcarCancelado(String pedidoId) {
        PedidoJpa pedido = pedido(pedidoId);
        pedido.mudarEstado(EstadoDoPedido.CANCELADO.name());
        pedidos.save(pedido);
    }

    private PedidoJpa pedido(String pedidoId) {
        return pedidos.findById(UUID.fromString(pedidoId))
                .orElseThrow(() -> new RegraDeNegocioException("pedido não encontrado"));
    }
}
