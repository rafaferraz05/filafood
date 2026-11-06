package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Linha do histórico de movimentações, pronta para a tela. */
public record MovimentacaoResumo(
        String tipo,
        String motivo,
        BigDecimal quantidade,
        BigDecimal custoUnitario,
        String origem,
        LocalDateTime dataHora) {
}
