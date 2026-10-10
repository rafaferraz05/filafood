package br.edu.cesar.filafood.dominio.estoque.devolucao;

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.EstoqueDaUnidade;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;

/**
 * Devolução ao estoque quando o preparo de um pedido é cancelado.
 *
 * Devolve exatamente as quantidades lançadas na baixa, lendo as saídas daquele pedido. A devolução
 * só é aceita para pedido em preparo: uma vez produzido, o que foi consumido não volta.
 */
public class DevolucaoDoPreparo {

    private final EstoqueDaUnidade estoque;

    public DevolucaoDoPreparo(EstoqueDaUnidade estoque) {
        this.estoque = estoque;
    }

    /**
     * @throws RegraDeNegocioException se o pedido não estiver em preparo. Nesse caso nada é gravado.
     */
    public void devolver(String pedido, EstadoDoPedido estado) {
        if (estado != EstadoDoPedido.EM_PREPARO) {
            throw new RegraDeNegocioException("a devolução só é aceita para pedido em preparo");
        }
        for (Insumo insumo : estoque.insumos()) {
            double consumido = insumo.consumidoPeloPedido(pedido);
            if (consumido > 0) {
                insumo.devolver(consumido, pedido);
            }
        }
    }
}
