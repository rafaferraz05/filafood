package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface EntregadorJpaRepository extends JpaRepository<EntregadorJpa, UUID> {

    List<EntregadorJpa> findByUnidadeId(UUID unidadeId);

    Optional<EntregadorJpa> findByUnidadeIdAndNome(UUID unidadeId, String nome);
}
