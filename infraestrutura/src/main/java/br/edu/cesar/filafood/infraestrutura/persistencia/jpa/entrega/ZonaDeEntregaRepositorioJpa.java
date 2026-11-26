package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntrega;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntregaRepositorio;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaId;

/** Implementação JPA da porta {@link ZonaDeEntregaRepositorio}. */
@Component
class ZonaDeEntregaRepositorioJpa implements ZonaDeEntregaRepositorio {

    private final ZonaEntregaJpaRepository repositorio;
    private final EntregaJpaMapeador mapeador;

    ZonaDeEntregaRepositorioJpa(ZonaEntregaJpaRepository repositorio, EntregaJpaMapeador mapeador) {
        this.repositorio = repositorio;
        this.mapeador = mapeador;
    }

    @Override
    public void salvar(ZonaDeEntrega zona) {
        ZonaEntregaJpa entidade = repositorio.findById(zona.id().valor())
                .orElseGet(() -> mapeador.novaEntidade(zona));
        mapeador.atualizar(entidade, zona);
        repositorio.save(entidade);
    }

    @Override
    public Optional<ZonaDeEntrega> porId(ZonaId id) {
        return repositorio.findById(id.valor()).map(mapeador::paraDominio);
    }

    @Override
    public Optional<ZonaDeEntrega> porNome(String unidadeId, String nome) {
        return repositorio.findByUnidadeIdAndNome(UUID.fromString(unidadeId), nome)
                .map(mapeador::paraDominio);
    }

    @Override
    public List<ZonaDeEntrega> daUnidade(String unidadeId) {
        return repositorio.findByUnidadeId(UUID.fromString(unidadeId)).stream()
                .map(mapeador::paraDominio)
                .toList();
    }
}
