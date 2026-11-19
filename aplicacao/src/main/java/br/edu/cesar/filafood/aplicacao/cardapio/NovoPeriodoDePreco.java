package br.edu.cesar.filafood.aplicacao.cardapio;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NovoPeriodoDePreco(
        String produtoId,
        BigDecimal preco,
        LocalDate inicio,
        LocalDate fim) {
}