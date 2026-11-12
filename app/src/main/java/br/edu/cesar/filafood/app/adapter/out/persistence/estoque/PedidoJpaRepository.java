package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface PedidoJpaRepository extends JpaRepository<PedidoJpa, UUID> {

    @EntityGraph(attributePaths = { "itens", "itens.produto" })
    @Override
    Optional<PedidoJpa> findById(UUID id);
}