package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/** Adaptador JPA da porta {@link Insumos} (docs/padroes-do-projeto.md, seção 8). */
@Component
class InsumosJpaAdapter implements Insumos {

    private final InsumoJpaRepository repositorio;
    private final InsumoMapper mapper;

    InsumosJpaAdapter(InsumoJpaRepository repositorio, InsumoMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Optional<Insumo> porId(InsumoId id) {
        return repositorio.findById(id.valor()).map(mapper::paraDominio);
    }

    @Override
    public List<Insumo> doEstoqueDaUnidade(String unidadeId) {
        return repositorio.findByUnidadeId(UUID.fromString(unidadeId)).stream()
                .map(mapper::paraDominio)
                .toList();
    }

    @Override
    public void salvar(Insumo insumo) {
        InsumoJpa entidade = repositorio.findById(insumo.id().valor())
                .orElseGet(() -> mapper.novaEntidade(insumo));
        mapper.atualizar(entidade, insumo);
        repositorio.save(entidade);
    }
}
