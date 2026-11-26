package br.edu.cesar.filafood.dominio.entrega.zona;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Política padrão da RN 3: valor base + quilômetros que excedem o raio grátis × valor por km,
 * limitado ao teto; zero quando o valor do pedido passa do mínimo da zona.
 */
public final class PoliticaDeTaxaPorZona implements PoliticaDeTaxa {

    @Override
    public BigDecimal calcular(ZonaDeEntrega zona, double distanciaKm, BigDecimal valorDoPedido) {
        if (valorDoPedido.compareTo(zona.valorMinimoDePedido()) > 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        double excedenteKm = Math.max(0, distanciaKm - zona.raioGratuitoKm());
        BigDecimal excedente = zona.valorPorKmExcedente().multiply(BigDecimal.valueOf(excedenteKm));
        return zona.valorBase().add(excedente).min(zona.teto()).setScale(2, RoundingMode.HALF_UP);
    }
}