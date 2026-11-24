package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.compras;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.compras.Cnpj;
import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.compras.Fornecedores;

/** Implementação JPA da porta {@link Fornecedores} (F7). */
@Component
public class FornecedoresJpaAdapter implements Fornecedores {

    private final FornecedorJpaRepository repositorio;

    public FornecedoresJpaAdapter(FornecedorJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Fornecedor> porId(FornecedorId id) {
        return repositorio.findWithItensById(id.valor()).map(FornecedorMapper::paraDominio);
    }

    @Override
    public Optional<Fornecedor> porCnpj(Cnpj cnpj) {
        return repositorio.findByCnpj(cnpj.valor()).map(FornecedorMapper::paraDominio);
    }

    @Override
    public List<Fornecedor> todos() {
        return repositorio.findAllByOrderByNomeAsc().stream().map(FornecedorMapper::paraDominio).toList();
    }

    /** Atualiza a entidade gerenciada quando ela existe; só cria uma nova no primeiro cadastro. */
    @Override
    public void salvar(Fornecedor fornecedor) {
        repositorio.findWithItensById(fornecedor.id().valor()).ifPresentOrElse(
                existente -> {
                    FornecedorMapper.atualizar(existente, fornecedor);
                    repositorio.save(existente);
                },
                () -> repositorio.save(FornecedorMapper.paraJpa(fornecedor)));
    }
}
