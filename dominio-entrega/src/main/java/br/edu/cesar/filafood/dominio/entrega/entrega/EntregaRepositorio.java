package br.edu.cesar.filafood.dominio.entrega.entrega;

import java.util.List;
import java.util.Optional;

/** Porta de persistência do agregado Entrega. */
public interface EntregaRepositorio {

    void salvar(Entrega entrega);

    /** Força o flush das gravações pendentes (usado antes da nova tentativa). */
    void flush();

    Optional<Entrega> porId(EntregaId id);

    List<Entrega> aguardando(String unidadeId);

    List<Entrega> ativasDaUnidade(String unidadeId);

    /** Última tentativa do pedido (a de maior número). */
    Optional<Entrega> doPedido(String pedido);

    List<Entrega> ativasDoEntregador(String entregadorId);
}