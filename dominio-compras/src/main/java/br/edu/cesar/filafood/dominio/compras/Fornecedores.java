package br.edu.cesar.filafood.dominio.compras;

import java.util.List;
import java.util.Optional;

/** Porta de repositório do agregado Fornecedor (F7). O adaptador JPA vive na infraestrutura. */
public interface Fornecedores {

    Optional<Fornecedor> porId(FornecedorId id);

    /** Usado na unicidade do CNPJ na rede (RN 1). */
    Optional<Fornecedor> porCnpj(Cnpj cnpj);

    /** Todos os fornecedores da rede, ativos e inativos, ordenados por nome. */
    List<Fornecedor> todos();

    void salvar(Fornecedor fornecedor);
}
