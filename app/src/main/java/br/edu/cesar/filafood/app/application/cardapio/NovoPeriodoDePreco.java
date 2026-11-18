package br.edu.cesar.filafood.app.application.cardapio;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NovoPeriodoDePreco(
        String produtoId,
        BigDecimal preco,
        LocalDate inicio,
        LocalDate fim) {
}