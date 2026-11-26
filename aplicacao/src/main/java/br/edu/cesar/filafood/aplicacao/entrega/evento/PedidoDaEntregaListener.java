package br.edu.cesar.filafood.aplicacao.entrega.evento;

import br.edu.cesar.filafood.aplicacao.entrega.pedido.PedidoDaEntregaRepositorioAplicacao;
import br.edu.cesar.filafood.dominio.entrega.entrega.DesfechoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.evento.EntregaFalhou;
import br.edu.cesar.filafood.dominio.entrega.entrega.evento.EventoDeEntrega;

/**
 * Observador que reage à falha da entrega: quando o desfecho é cancelamento, atualiza o pedido do
 * contexto de atendimento. O serviço de entrega não conhece essa consequência.
 */
public class PedidoDaEntregaListener implements OuvinteDeEventosDeEntrega {

    private final PedidoDaEntregaRepositorioAplicacao pedidos;

    public PedidoDaEntregaListener(PedidoDaEntregaRepositorioAplicacao pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public void ao(EventoDeEntrega evento) {
        if (evento instanceof EntregaFalhou falhou && falhou.desfecho() == DesfechoDaFalha.PEDIDO_CANCELADO) {
            pedidos.marcarCancelado(falhou.pedido());
        }
    }
}
