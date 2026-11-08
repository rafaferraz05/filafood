package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface FichaTecnicaJpaRepository extends JpaRepository<FichaTecnicaJpa, UUID> {

    /**
     * O mapper percorre itens, insumo e movimentações do insumo depois que a consulta termina; sem
     * o grafo, a coleção preguiçosa estoura fora da sessão.
     */
    @EntityGraph(attributePaths = { "produto", "itens", "itens.insumo", "itens.insumo.movimentacoes" })
    List<FichaTecnicaJpa> findByUnidadeId(UUID unidadeId);

    @EntityGraph(attributePaths = { "produto", "itens", "itens.insumo", "itens.insumo.movimentacoes" })
    Optional<FichaTecnicaJpa> findByUnidadeIdAndProduto_Id(UUID unidadeId, UUID produtoId);
}
