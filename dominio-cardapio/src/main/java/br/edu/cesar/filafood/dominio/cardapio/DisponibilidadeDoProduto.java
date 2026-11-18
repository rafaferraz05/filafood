package br.edu.cesar.filafood.dominio.cardapio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Cálculo de disponibilidade do produto a partir dos insumos da ficha (F6 RN 5).
 * Não acopla ao módulo dominio-estoque: recebe apenas os dados necessários para o cálculo.
 */
public final class DisponibilidadeDoProduto {

    public record ItemInsumo(String nome, BigDecimal porcao, BigDecimal saldo, boolean ativo) {}

    public record Resultado(int porcoesPossiveis, boolean disponivel) {}

    private DisponibilidadeDoProduto() {
    }

    public static Resultado calcular(List<ItemInsumo> itensDaFicha) {
        if (itensDaFicha == null || itensDaFicha.isEmpty()) {
            return new Resultado(0, false);
        }
        for (ItemInsumo item : itensDaFicha) {
            if (!item.ativo() || item.saldo() == null || item.saldo().signum() <= 0
                    || item.porcao() == null || item.porcao().signum() <= 0) {
                return new Resultado(0, false);
            }
        }
        int porcoes = itensDaFicha.stream()
                .mapToInt(item -> item.saldo().divide(item.porcao(), 0, RoundingMode.FLOOR).intValue())
                .min()
                .orElse(0);
        return new Resultado(porcoes, porcoes >= 1);
    }
}