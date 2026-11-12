package br.edu.cesar.filafood.dominio.estoque.ficha;

import java.math.BigDecimal;
import java.util.Iterator;
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
 *
 * É {@link Iterable} de propósito: a explosão do pedido e a tela de ficha técnica percorrem os itens
 * pelo iterador, sem receber a coleção interna (ADR 0010).
 *
 * O produto é referenciado por {@link ProdutoId}: o cardápio é do contexto de F6, e a ficha guarda
 * identidade, não uma cópia do nome.
 */
public class FichaTecnica implements Iterable<ItemDaFicha> {

    private final FichaTecnicaId id;
    private final String unidadeId;
    private final ProdutoId produto;
    private final Map<String, ItemDaFicha> itensPorInsumo = new LinkedHashMap<>();

    public FichaTecnica(FichaTecnicaId id, String unidadeId, ProdutoId produto) {
        if (id == null) {
            throw new IllegalArgumentException("o id da ficha técnica é obrigatório");
        }
        if (produto == null) {
            throw new RegraDeNegocioException("informe o produto da ficha técnica");
        }
        this.id = id;
        this.unidadeId = unidadeId;
        this.produto = produto;
    }

    /**
     * Reconstitui a ficha a partir da persistência, sem repetir as validações de edição: os itens já
     * foram validados quando entraram.
     */
    public static FichaTecnica reconstituir(FichaTecnicaId id, String unidadeId, ProdutoId produto,
            List<ItemDaFicha> itens) {
        FichaTecnica ficha = new FichaTecnica(id, unidadeId, produto);
        itens.forEach(item -> ficha.itensPorInsumo.put(item.insumo().nome(), item));
        return ficha;
    }

    public void definirItem(Insumo insumo, BigDecimal quantidade) {
        if (!insumo.isAtivo()) {
            throw new RegraDeNegocioException("o insumo está inativo");
        }
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraDeNegocioException("a quantidade deve ser maior que zero");
        }
        if (itensPorInsumo.containsKey(insumo.nome())) {
            throw new RegraDeNegocioException("o insumo já está na ficha técnica");
        }
        itensPorInsumo.put(insumo.nome(), new ItemDaFicha(insumo, quantidade));
    }

    /** Remove o insumo da ficha; a tela de ficha técnica oferece a ação por item. */
    public void removerItem(Insumo insumo) {
        if (itensPorInsumo.remove(insumo.nome()) == null) {
            throw new RegraDeNegocioException("o insumo não está na ficha técnica");
        }
    }

    public int quantidadeDeItens() {
        return itensPorInsumo.size();
    }

    public List<ItemDaFicha> itens() {
        return List.copyOf(itensPorInsumo.values());
    }

    public FichaTecnicaId id() {
        return id;
    }

    public String unidadeId() {
        return unidadeId;
    }

    public ProdutoId produto() {
        return produto;
    }

    @Override
    public Iterator<ItemDaFicha> iterator() {
        return itens().iterator();
    }
}