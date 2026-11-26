package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ZonaEntregaJpaRepository extends JpaRepository<ZonaEntregaJpa, UUID> {

    List<ZonaEntregaJpa> findByUnidadeId(UUID unidadeId);

    Optional<ZonaEntregaJpa> findByUnidadeIdAndNome(UUID unidadeId, String nome);
}
