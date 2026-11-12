package br.edu.cesar.filafood.app.application.estoque;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/** Conclusão do preparo: não movimenta estoque, só fecha o estado do pedido (RN 4). */
@Service
public class ConcluirPreparo {

    private final PedidoDoPreparo pedidos;

    public ConcluirPreparo(PedidoDoPreparo pedidos) {
        this.pedidos = pedidos;
    }

    @Transactional
    public void executar(String pedidoId) {
        PedidoParaPreparo pedido = pedidos.porId(pedidoId);
        if (pedido.estado() != EstadoDoPedido.EM_PREPARO) {
            throw new RegraDeNegocioException("o preparo só pode ser concluído com o pedido em preparo");
        }
        pedidos.marcarConcluido(pedidoId);
    }
}