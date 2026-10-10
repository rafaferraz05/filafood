package br.edu.cesar.filafood.dominio.entrega.entrega;

import java.time.LocalDateTime;

/** Registro de uma transição de estado. Toda transição grava o estado e o momento. */
public record TransicaoDaEntrega(EstadoDaEntrega estado, LocalDateTime dataHora) {
}
