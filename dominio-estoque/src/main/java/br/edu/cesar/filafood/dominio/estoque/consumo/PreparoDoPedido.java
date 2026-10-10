package br.edu.cesar.filafood.dominio.estoque.consumo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.edu.cesar.filafood.dominio.estoque.EstoqueDaUnidade;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicasDaUnidade;
import br.edu.cesar.filafood.dominio.estoque.ficha.ItemDaFicha;

/**
 * Baixa automática por explosão da ficha técnica.
 *
 * Ao iniciar o preparo de um pedido, calcula a necessidade de cada insumo e dá baixa. A operação
 * é de tudo ou nada: se faltar saldo de qualquer insumo, nenhuma movimentação é gravada, para que
 * o pedido não consuma metade do que precisa.
 */
public class PreparoDoPedido {

    private final EstoqueDaUnidade estoque;
    private final FichasTecnicasDaUnidade fichas;

    public PreparoDoPedido(EstoqueDaUnidade estoque, FichasTecnicasDaUnidade fichas) {
        this.estoque = estoque;
        this.fichas = fichas;
    }

    /**
     * Calcula a necessidade por insumo e dá baixa.
     *
     * @throws RegraDeNegocioException se algum produto do pedido não tiver ficha técnica na unidade,
     *         ou se faltar saldo de qualquer insumo. Nesse caso nada é gravado.
     */
    public void darBaixa(String pedido, List<ItemDoPedido> itens) {
        Map<String, Double> necessidade = necessidade(itens);

        for (Map.Entry<String, Double> linha : necessidade.entrySet()) {
            if (estoque.insumo(linha.getKey()).saldo() < linha.getValue()) {
                throw new RegraDeNegocioException("falta de saldo do insumo " + linha.getKey());
            }
        }

        necessidade.forEach((insumo, quantidade) -> estoque.insumo(insumo).consumir(quantidade, pedido));
    }

    /**
     * Necessidade de cada insumo para o pedido: para cada item, multiplica a quantidade pedida pela
     * quantidade do insumo na ficha técnica do produto, somando as contribuições dos itens repetidos.
     */
    public Map<String, Double> necessidade(List<ItemDoPedido> itens) {
        Map<String, Double> necessidade = new LinkedHashMap<>();
        for (ItemDoPedido item : itens) {
            FichaTecnica ficha = fichas.exigirFichaDe(item.produto());
            for (ItemDaFicha daFicha : ficha.itens()) {
                necessidade.merge(daFicha.insumo().nome(), daFicha.quantidade() * item.quantidade(), Double::sum);
            }
        }
        return necessidade;
    }
}
