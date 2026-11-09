package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * O grafo carrega a ficha e os itens numa consulta. As movimentações dos insumos ficam para o
 * carregamento preguiçoso dentro da transação do caso de uso: juntar os dois "bags" na mesma
 * consulta faria o Hibernate recusar com {@code MultipleBagFetchException}.
 */
interface FichaTecnicaJpaRepository extends JpaRepository<FichaTecnicaJpa, UUID> {

    @EntityGraph(attributePaths = { "produto", "itens" })
    List<FichaTecnicaJpa> findByUnidadeId(UUID unidadeId);

    @EntityGraph(attributePaths = { "produto", "itens" })
    Optional<FichaTecnicaJpa> findByUnidadeIdAndProduto_Id(UUID unidadeId, UUID produtoId);
}
