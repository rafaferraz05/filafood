package br.edu.cesar.filafood.dominio.compras;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Sugestão de quantidade a comprar (F7 RN 3).
 *
 * <p>Quantidade bruta = ponto de pedido + consumo médio diário × dias de cobertura − saldo −
 * quantidade em ordens abertas. Bruta menor ou igual a zero não gera sugestão. O arredondamento ao
 * múltiplo da quantidade mínima depende do fornecedor e é feito na escolha (RN 4).
 *
 * <p>Recebe só números: não acopla ao módulo de estoque. Quem lê saldo, consumo e ponto de pedido do
 * insumo real é o caso de uso.
 */
public final class SugestaoDeCompra {

    private static final int ESCALA_DA_QUANTIDADE = 3;

    public record Resultado(BigDecimal quantidadeBruta, BigDecimal quantidadeSugerida) {

        public boolean haSugestao() {
            return quantidadeSugerida.signum() > 0;
        }
    }

    private SugestaoDeCompra() {
    }

    public static Resultado calcular(BigDecimal pontoDePedido, BigDecimal consumoMedioDiario, int cobertura,
            BigDecimal saldo, BigDecimal emOrdensAbertas) {
        if (cobertura < 1) {
            throw new RegraDeNegocioException("a cobertura deve ser de ao menos 1 dia");
        }
        BigDecimal bruta = valor(pontoDePedido)
                .add(valor(consumoMedioDiario).multiply(BigDecimal.valueOf(cobertura)))
                .subtract(valor(saldo))
                .subtract(valor(emOrdensAbertas))
                .setScale(ESCALA_DA_QUANTIDADE, RoundingMode.HALF_UP);
        BigDecimal sugerida = bruta.signum() <= 0 ? BigDecimal.ZERO.setScale(ESCALA_DA_QUANTIDADE) : bruta;
        return new Resultado(bruta, sugerida);
    }

    private static BigDecimal valor(BigDecimal numero) {
        return numero == null ? BigDecimal.ZERO : numero;
    }
}
