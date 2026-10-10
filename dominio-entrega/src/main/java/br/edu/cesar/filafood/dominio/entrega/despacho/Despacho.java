package br.edu.cesar.filafood.dominio.entrega.despacho;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.entrega.Entrega;

/**
 * Escolha do entregador para um pedido pronto.
 *
 * <p>É elegível o entregador ativo, que atende a zona do endereço e cuja carga em andamento é menor
 * que a carga máxima. Entre os elegíveis, escolhe-se o de menor carga e, em empate, o que está há
 * mais tempo sem receber entrega. Sem elegível, o pedido permanece na fila de despacho.
 */
public class Despacho {

    public Optional<Entregador> escolher(String zona, List<Entregador> entregadores) {
        return entregadores.stream()
                .filter(Entregador::isAtivo)
                .filter(entregador -> entregador.atende(zona))
                .filter(Entregador::temCargaDisponivel)
                .min(Comparator.comparingInt(Entregador::cargaEmAndamento)
                        .thenComparing(Entregador::ultimaEntregaEm,
                                Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    /**
     * Despacha a entrega para o entregador escolhido, ou a deixa na fila quando não há elegível.
     *
     * @return verdadeiro quando houve despacho.
     */
    public boolean despachar(Entrega entrega, FilaDeDespacho fila, List<Entregador> entregadores) {
        Optional<Entregador> escolhido = escolher(entrega.zona().nome(), entregadores);
        if (escolhido.isEmpty()) {
            return false;
        }
        Entregador entregador = escolhido.get();
        entrega.despachar(entregador);
        entregador.assumirEntrega(LocalDateTime.now());
        fila.remover(entrega);
        return true;
    }
}
