package br.edu.cesar.filafood.dominio.entrega.despacho;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;

/**
 * Política padrão da RN 4: ativo, atende a zona, carga abaixo da máxima; menor carga e, no empate,
 * quem está há mais tempo sem receber entrega.
 */
public final class MenorCargaMaisAntiga implements PoliticaDeEscolhaDeEntregador {

    @Override
    public Optional<Entregador> escolher(String zona, List<Entregador> entregadores) {
        return entregadores.stream()
                .filter(Entregador::isAtivo)
                .filter(entregador -> entregador.atende(zona))
                .filter(Entregador::temCargaDisponivel)
                .min(Comparator.comparingInt(Entregador::cargaEmAndamento)
                        .thenComparing(Entregador::ultimaEntregaEm,
                                Comparator.nullsFirst(Comparator.naturalOrder())));
    }
}