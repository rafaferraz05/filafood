package br.edu.cesar.filafood.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProdutoJpaRepository extends JpaRepository<ProdutoJpa, UUID> {

    List<ProdutoJpa> findByUnidadeId(UUID unidadeId);
}
