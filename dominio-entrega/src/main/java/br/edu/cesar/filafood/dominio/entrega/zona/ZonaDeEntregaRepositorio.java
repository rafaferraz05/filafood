package br.edu.cesar.filafood.dominio.entrega.zona;

import java.util.List;
import java.util.Optional;

/** Porta de persistência do agregado ZonaDeEntrega. */
public interface ZonaDeEntregaRepositorio {

    void salvar(ZonaDeEntrega zona);

    Optional<ZonaDeEntrega> porId(ZonaId id);

    Optional<ZonaDeEntrega> porNome(String unidadeId, String nome);

    List<ZonaDeEntrega> daUnidade(String unidadeId);
}