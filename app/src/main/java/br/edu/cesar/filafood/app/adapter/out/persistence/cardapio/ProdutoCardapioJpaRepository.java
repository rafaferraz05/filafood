package br.edu.cesar.filafood.app.adapter.out.persistence.cardapio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoCardapioJpaRepository extends JpaRepository<ProdutoCardapioJpa, UUID> {

    @EntityGraph(attributePaths = "periodos")
    Optional<ProdutoCardapioJpa> findWithPeriodosById(UUID id);

    @EntityGraph(attributePaths = "periodos")
    Optional<ProdutoCardapioJpa> findByUnidadeIdAndNomeIgnoreCase(UUID unidadeId, String nome);

    @EntityGraph(attributePaths = "periodos")
    List<ProdutoCardapioJpa> findByUnidadeIdOrderByCategoriaAscNomeAsc(UUID unidadeId);
}