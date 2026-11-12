package br.edu.cesar.filafood.dominio.estoque.consumo;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.edu.cesar.filafood.dominio.estoque.EstoqueDaUnidade;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicasDaUnidade;

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
        List<NecessidadeDoPedido.Linha> linhas = necessidade(itens).linhas();

        for (NecessidadeDoPedido.Linha linha : linhas) {
            if (linha.insumo().saldo().compareTo(linha.quantidade()) < 0) {
                throw new RegraDeNegocioException("falta de saldo do insumo " + linha.insumo().nome());
            }
        }

        linhas.forEach(linha -> linha.insumo().consumir(linha.quantidade(), pedido));
    }

    /**
     * Necessidade de cada insumo para o pedido, por nome, como o dublê de teste consulta.
     */
    public Map<String, BigDecimal> necessidadePorNome(List<ItemDoPedido> itens) {
        Map<String, BigDecimal> porNome = new LinkedHashMap<>();
        for (NecessidadeDoPedido.Linha linha : necessidade(itens).linhas()) {
            porNome.put(linha.insumo().nome(), linha.quantidade());
        }
        return porNome;
    }

    /** Explosão da ficha para o pedido, compartilhada com a aplicação. */
    public NecessidadeDoPedido necessidade(List<ItemDoPedido> itens) {
        return new NecessidadeDoPedido(itens, fichas::exigirFichaDe);
    }
}