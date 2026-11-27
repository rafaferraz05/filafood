package br.edu.cesar.filafood.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpa, UUID> {

    @EntityGraph(attributePaths = { "itens", "itens.produto" })
    @Override
    Optional<PedidoJpa> findById(UUID id);

    @EntityGraph(attributePaths = { "itens", "itens.produto" })
    List<PedidoJpa> findByUnidadeId(UUID unidadeId);
}