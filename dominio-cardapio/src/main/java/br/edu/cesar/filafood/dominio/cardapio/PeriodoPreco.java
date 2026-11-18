package br.edu.cesar.filafood.dominio.cardapio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Período de preço próprio da unidade (F6 RN 4).
 * Fim nulo representa vigência sem data final ("sem fim").
 */
public record PeriodoPreco(UUID id, BigDecimal preco, LocalDate inicio, LocalDate fim) {

    public PeriodoPreco {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (preco == null || preco.signum() <= 0) {
            throw new RegraDeNegocioException("o preço deve ser maior que zero");
        }
        if (inicio == null) {
            throw new RegraDeNegocioException("informe a data inicial");
        }
        if (fim != null && fim.isBefore(inicio)) {
            throw new RegraDeNegocioException("a data final não pode ser anterior à data inicial");
        }
        preco = preco.setScale(2, RoundingMode.HALF_UP);
    }

    public static PeriodoPreco novo(BigDecimal preco, LocalDate inicio, LocalDate fim) {
        return new PeriodoPreco(UUID.randomUUID(), preco, inicio, fim);
    }

    public boolean contem(LocalDate data) {
        return !data.isBefore(inicio) && (fim == null || !data.isAfter(fim));
    }

    public boolean sobrepoe(LocalDate outroInicio, LocalDate outroFim) {
        LocalDate fimEfetivo = fim == null ? LocalDate.MAX : fim;
        LocalDate outroFimEfetivo = outroFim == null ? LocalDate.MAX : outroFim;
        return !inicio.isAfter(outroFimEfetivo) && !outroInicio.isAfter(fimEfetivo);
    }
}