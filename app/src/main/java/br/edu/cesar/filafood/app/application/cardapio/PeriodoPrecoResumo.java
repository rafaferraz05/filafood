package br.edu.cesar.filafood.app.application.cardapio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PeriodoPrecoResumo(
        UUID id,
        BigDecimal preco,
        LocalDate inicio,
        LocalDate fim,
        boolean vigenteHoje) {
}   