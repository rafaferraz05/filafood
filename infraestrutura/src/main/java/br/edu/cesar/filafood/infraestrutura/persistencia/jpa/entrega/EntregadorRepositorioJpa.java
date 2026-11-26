package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorId;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorRepositorio;

/** Implementação JPA da porta {@link EntregadorRepositorio}. */
@Component
class EntregadorRepositorioJpa implements EntregadorRepositorio {

    private final EntregadorJpaRepository repositorio;
    private final EntregaJpaMapeador mapeador;

    EntregadorRepositorioJpa(EntregadorJpaRepository repositorio, EntregaJpaMapeador mapeador) {
        this.repositorio = repositorio;
        this.mapeador = mapeador;
    }

    @Override
    public void salvar(Entregador entregador) {
        EntregadorJpa entidade = repositorio.findById(entregador.id().valor())
                .orElseGet(() -> mapeador.novaEntidade(entregador));
        mapeador.atualizar(entidade, entregador);
        repositorio.save(entidade);
    }

    @Override
    public Optional<Entregador> porId(EntregadorId id) {
        return repositorio.findById(id.valor()).map(mapeador::paraDominio);
    }

    @Override
    public Optional<Entregador> porNome(String unidadeId, String nome) {
        return repositorio.findByUnidadeIdAndNome(UUID.fromString(unidadeId), nome)
                .map(mapeador::paraDominio);
    }

    @Override
    public List<Entregador> daUnidade(String unidadeId) {
        return repositorio.findByUnidadeId(UUID.fromString(unidadeId)).stream()
                .map(mapeador::paraDominio)
                .toList();
    }
}
