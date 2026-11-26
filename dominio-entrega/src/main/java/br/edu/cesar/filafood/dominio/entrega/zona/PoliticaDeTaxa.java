package br.edu.cesar.filafood.dominio.entrega.zona;

import java.math.BigDecimal;

/**
 * Estratégia do cálculo da taxa de entrega (RN 3).
 *
 * A política recebe a zona e os dados do pedido; trocar a fórmula é trocar a implementação, sem
 * tocar no despacho nem na entrega (ADR 0010/0013).
 */
public interface PoliticaDeTaxa {

    BigDecimal calcular(ZonaDeEntrega zona, double distanciaKm, BigDecimal valorDoPedido);
}