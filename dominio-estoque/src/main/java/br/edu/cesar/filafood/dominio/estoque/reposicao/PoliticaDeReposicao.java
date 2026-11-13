package br.edu.cesar.filafood.dominio.estoque.reposicao;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;

/**
 * Estratégia do ponto de pedido e do sinal de reposição (RN 6).
 *
 * Fica no domínio porque é regra de negócio pura: a aplicação injeta a implementação e o BDD usa a
 * mesma conta. Trocar a fórmula é trocar a implementação, sem tocar nos casos de uso (ADR 0010).
 */
public interface PoliticaDeReposicao {

    /** Ponto de pedido do insumo na data de referência. */
    BigDecimal pontoDePedido(Insumo insumo, LocalDate referencia);

    /** O insumo está a repor quando o saldo não cobre o ponto de pedido. */
    boolean estaARepor(Insumo insumo, LocalDate referencia);
}