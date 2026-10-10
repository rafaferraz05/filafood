package br.edu.cesar.filafood.dominio.estoque.ficha;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;

/**
 * Ficha técnica de um produto em uma unidade: quais insumos, e em que quantidade, o produto consome
 * no preparo de uma porção.
 *
 * É a única ponte entre produto, que é o que se vende, e insumo, que é o que se consome.
 */
public class FichaTecnica {

    private final String produto;
    private final Map<String, ItemDaFicha> itensPorInsumo = new LinkedHashMap<>();

    public FichaTecnica(String produto) {
        this.produto = produto;
    }

    public void definirItem(Insumo insumo, double quantidade) {
        if (!insumo.isAtivo()) {
            throw new RegraDeNegocioException("o insumo está inativo");
        }
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("a quantidade deve ser maior que zero");
        }
        if (itensPorInsumo.containsKey(insumo.nome())) {
            throw new RegraDeNegocioException("o insumo já está na ficha técnica");
        }
        itensPorInsumo.put(insumo.nome(), new ItemDaFicha(insumo, quantidade));
    }

    public int quantidadeDeItens() {
        return itensPorInsumo.size();
    }

    public List<ItemDaFicha> itens() {
        return List.copyOf(itensPorInsumo.values());
    }

    public String produto() {
        return produto;
    }
}
