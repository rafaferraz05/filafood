package br.edu.cesar.filafood.dominio.entrega.entregador;

import java.util.List;
import java.util.Optional;

/** Porta de persistência do agregado Entregador. */
public interface EntregadorRepositorio {

    void salvar(Entregador entregador);

    Optional<Entregador> porId(EntregadorId id);

    Optional<Entregador> porNome(String unidadeId, String nome);

    List<Entregador> daUnidade(String unidadeId);
}