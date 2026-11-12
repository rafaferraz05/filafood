package br.edu.cesar.filafood.dominio.estoque.ficha;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/**
 * Fichas técnicas mantidas pela unidade, uma por produto.
 *
 * O produto em si pertence ao cardápio da rede, que é de outro contexto. Aqui só interessa que
 * exista, ou não, uma ficha técnica dele nesta unidade.
 *
 * A fixture fala o nome do produto (é o que a Gherkin diz) e guarda a identidade por trás, para o
 * agregado trabalhar só com {@link ProdutoId}.
 */
public class FichasTecnicasDaUnidade {

    private final String unidadeId;
    private final Map<String, ProdutoId> idsPorProduto = new LinkedHashMap<>();
    private final Map<ProdutoId, FichaTecnica> porProduto = new LinkedHashMap<>();

    /** Fichas da unidade; o id é gerado quando o chamador não informa (dublês de teste). */
    public FichasTecnicasDaUnidade() {
        this(UUID.randomUUID().toString());
    }

    public FichasTecnicasDaUnidade(String unidadeId) {
        this.unidadeId = unidadeId;
    }

    /** Abre a ficha do produto para edição, criando-a se ainda não existir. */
    public FichaTecnica definir(String produto) {
        return porProduto.computeIfAbsent(idDe(produto),
                id -> new FichaTecnica(FichaTecnicaId.novo(), unidadeId, id));
    }

    /** A ficha do produto nesta unidade. Produto sem ficha técnica não pode ser vendido. */
    public FichaTecnica exigirFichaDe(String produto) {
        FichaTecnica ficha = porProduto.get(idsPorProduto.get(produto));
        if (ficha == null) {
            throw new RegraDeNegocioException("produto sem ficha técnica na unidade");
        }
        return ficha;
    }

    public boolean temFichaDe(String produto) {
        return porProduto.containsKey(idsPorProduto.get(produto));
    }

    private ProdutoId idDe(String produto) {
        return idsPorProduto.computeIfAbsent(produto, nome -> ProdutoId.novo());
    }
}