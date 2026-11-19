package br.edu.cesar.filafood.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoRepositorio;

/** Implementação JPA da porta {@link InsumoRepositorio}. */
@Component
class InsumoRepositorioJpa implements InsumoRepositorio {

    private final InsumoJpaRepository repositorio;
    private final JpaMapeador mapeador;

    InsumoRepositorioJpa(InsumoJpaRepository repositorio, JpaMapeador mapeador) {
        this.repositorio = repositorio;
        this.mapeador = mapeador;
    }

    @Override
    public Optional<Insumo> porId(InsumoId id) {
        return repositorio.findById(id.valor()).map(mapeador::paraDominio);
    }

    @Override
    public Optional<Insumo> porNome(String unidadeId, String nome) {
        return repositorio.findByUnidadeIdAndNome(UUID.fromString(unidadeId), nome).map(mapeador::paraDominio);
    }

    @Override
    public List<Insumo> doEstoqueDaUnidade(String unidadeId) {
        return repositorio.findByUnidadeId(UUID.fromString(unidadeId)).stream()
                .map(mapeador::paraDominio)
                .toList();
    }

    @Override
    public void salvar(Insumo insumo) {
        InsumoJpa entidade = repositorio.findById(insumo.id().valor())
                .orElseGet(() -> mapeador.novaEntidade(insumo));
        mapeador.atualizar(entidade, insumo);
        repositorio.save(entidade);
    }
}