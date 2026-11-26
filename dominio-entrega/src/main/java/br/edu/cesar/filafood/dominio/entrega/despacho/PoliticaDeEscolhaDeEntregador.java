package br.edu.cesar.filafood.dominio.entrega.despacho;

import java.util.List;
import java.util.Optional;

import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;

/**
 * Estratégia da escolha do entregador (RN 4).
 *
 * A implementação decide elegibilidade e desempate; o despacho só pede o escolhido e grava o
 * resultado. Uma nova regra de distribuição entra sem mexer no fluxo de despacho.
 */
public interface PoliticaDeEscolhaDeEntregador {

    Optional<Entregador> escolher(String zona, List<Entregador> entregadores);
}