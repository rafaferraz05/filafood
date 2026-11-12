package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/**
 * Devolução ao cancelar o preparo (F1 RN 5).
 *
 * Devolve exatamente o que a baixa lançou, lendo as saídas daquele pedido. Só é aceita para pedido
 * em preparo: concluído, recusado ou já cancelado não devolve nada — e como a segunda tentativa
 * encontra o pedido cancelado, o cancelamento não devolve duas vezes.
 */
@Service
public class DevolucaoDoCancelamento extends MovimentacaoNoEstoque<CancelamentoDoPreparo> {

    private final PedidoDoPreparo pedidos;

    public DevolucaoDoCancelamento(Insumos insumos, PedidoDoPreparo pedidos) {
        super(insumos);
        this.pedidos = pedidos;
    }

    @Override
    protected List<LinhaDaMovimentacao> linhas(CancelamentoDoPreparo comando) {
        PedidoParaPreparo pedido = pedidos.porId(comando.pedidoId());
        if (pedido.estado() != EstadoDoPedido.EM_PREPARO) {
            throw new RegraDeNegocioException("a devolução só é aceita para pedido em preparo");
        }
        List<LinhaDaMovimentacao> linhas = new ArrayList<>();
        for (Insumo insumo : insumos.doEstoqueDaUnidade(pedido.unidadeId())) {
            BigDecimal consumido = insumo.consumidoPeloPedido(comando.pedidoId());
            if (consumido.signum() > 0) {
                linhas.add(new LinhaDaMovimentacao(insumo, consumido));
            }
        }
        return linhas;
    }

    @Override
    protected void aplicar(LinhaDaMovimentacao linha, CancelamentoDoPreparo comando) {
        linha.insumo().devolver(linha.quantidade(), comando.pedidoId());
    }

    @Override
    protected void concluir(CancelamentoDoPreparo comando) {
        pedidos.marcarCancelado(comando.pedidoId());
    }
}