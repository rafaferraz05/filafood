package br.edu.cesar.filafood.aplicacao.entrega.entrega;

import java.time.LocalDateTime;

import br.edu.cesar.filafood.dominio.entrega.entrega.EstadoDaEntrega;

public record ItemDaTimeline(EstadoDaEntrega estado, LocalDateTime dataHora) {
}