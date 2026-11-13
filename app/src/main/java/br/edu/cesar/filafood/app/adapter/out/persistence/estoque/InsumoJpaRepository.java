package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface InsumoJpaRepository extends JpaRepository<InsumoJpa, UUID> {

    /**
     * O mapper percorre as movimentações depois que a consulta termina; sem o grafo, a coleção
     * preguiçosa estoura {@code LazyInitializationException} quando não há sessão aberta.
     */
    @Override
    @EntityGraph(attributePaths = "movimentacoes")
    Optional<InsumoJpa> findById(UUID id);

    @EntityGraph(attributePaths = "movimentacoes")
    List<InsumoJpa> findByUnidadeId(UUID unidadeId);

    Optional<InsumoJpa> findByUnidadeIdAndNome(UUID unidadeId, String nome);
}
