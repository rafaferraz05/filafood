package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProdutoJpaRepository extends JpaRepository<ProdutoJpa, UUID> {

    List<ProdutoJpa> findByUnidadeId(UUID unidadeId);
}
