package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface InsumoJpaRepository extends JpaRepository<InsumoJpa, UUID> {

    List<InsumoJpa> findByUnidadeId(UUID unidadeId);
}
