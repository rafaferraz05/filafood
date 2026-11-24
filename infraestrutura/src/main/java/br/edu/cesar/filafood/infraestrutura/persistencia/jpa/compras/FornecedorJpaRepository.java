package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.compras;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorJpaRepository extends JpaRepository<FornecedorJpa, UUID> {

    @EntityGraph(attributePaths = "itens")
    Optional<FornecedorJpa> findWithItensById(UUID id);

    @EntityGraph(attributePaths = "itens")
    Optional<FornecedorJpa> findByCnpj(String cnpj);

    @EntityGraph(attributePaths = "itens")
    List<FornecedorJpa> findAllByOrderByNomeAsc();
}
