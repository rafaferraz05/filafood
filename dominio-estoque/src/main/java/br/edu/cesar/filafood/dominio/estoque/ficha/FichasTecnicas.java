package br.edu.cesar.filafood.dominio.estoque.ficha;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída das fichas técnicas. O adaptador JPA vive no `app`; o domínio não conhece
 * framework (docs/padroes-do-projeto.md, seção 4).
 */
public interface FichasTecnicas {

    Optional<FichaTecnica> porProduto(String unidadeId, String produto);

    List<FichaTecnica> daUnidade(String unidadeId);

    void salvar(FichaTecnica ficha);

    /**
     * Remove a ficha. Ficha sem itens é igual a produto sem ficha: o produto volta ao estado "sem
     * ficha" do protótipo e não pode ser vendido.
     */
    void remover(FichaTecnica ficha);
}
