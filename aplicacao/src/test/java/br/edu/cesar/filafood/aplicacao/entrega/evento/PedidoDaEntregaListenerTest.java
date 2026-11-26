package br.edu.cesar.filafood.aplicacao.entrega.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.aplicacao.entrega.pedido.PedidoDaEntrega;
import br.edu.cesar.filafood.aplicacao.entrega.pedido.PedidoDaEntregaRepositorioAplicacao;
import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.entrega.DesfechoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaId;
import br.edu.cesar.filafood.dominio.entrega.entrega.MotivoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.evento.EntregaFalhou;

/** Observador da falha: só cancela o pedido quando o desfecho é cancelamento. */
class PedidoDaEntregaListenerTest {

    @Test
    void cancelaOPedidoQuandoODesfechoEhCancelamento() {
        PedidoFake pedidos = new PedidoFake();
        new PedidoDaEntregaListener(pedidos).ao(new EntregaFalhou(EntregaId.novo(), "P1",
                MotivoDaFalha.RECUSA_DO_CLIENTE, "cliente recusou", DesfechoDaFalha.PEDIDO_CANCELADO));
        assertEquals("P1", pedidos.cancelado);
    }

    @Test
    void naoCancelaQuandoHaNovaTentativa() {
        PedidoFake pedidos = new PedidoFake();
        new PedidoDaEntregaListener(pedidos).ao(new EntregaFalhou(EntregaId.novo(), "P1",
                MotivoDaFalha.AUSENCIA_DO_CLIENTE, "ninguém atendeu", DesfechoDaFalha.NOVA_TENTATIVA));
        assertNull(pedidos.cancelado);
    }

    private static final class PedidoFake implements PedidoDaEntregaRepositorioAplicacao {

        private String cancelado;

        @Override
        public PedidoDaEntrega porId(String pedidoId) {
            return new PedidoDaEntrega(pedidoId, "U1", EstadoDoPedido.CONCLUIDO, java.math.BigDecimal.ONE,
                    new Coordenada(0, 0), "endereço");
        }

        @Override
        public void marcarCancelado(String pedidoId) {
            cancelado = pedidoId;
        }
    }
}
