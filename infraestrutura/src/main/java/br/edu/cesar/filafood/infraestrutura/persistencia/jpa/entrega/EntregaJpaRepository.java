package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface EntregaJpaRepository extends JpaRepository<EntregaJpa, UUID> {

    @EntityGraph(attributePaths = { "pedido", "zona", "entregador", "transicoes" })
    List<EntregaJpa> findByUnidadeIdAndEstadoOrderByCriadoEmAsc(UUID unidadeId, String estado);

    @EntityGraph(attributePaths = { "pedido", "zona", "entregador", "transicoes" })
    List<EntregaJpa> findByUnidadeIdAndEstadoIn(UUID unidadeId, Collection<String> estados);

    @EntityGraph(attributePaths = { "pedido", "zona", "entregador", "transicoes" })
    Optional<EntregaJpa> findFirstByPedido_IdOrderByTentativaDesc(UUID pedidoId);

    @EntityGraph(attributePaths = { "pedido", "zona", "entregador", "transicoes" })
    List<EntregaJpa> findByEntregador_IdAndEstadoIn(UUID entregadorId, Collection<String> estados);
}
