package br.edu.cesar.filafood.aplicacao.entrega.entregador;

import java.time.LocalDateTime;
import java.util.Set;

import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorId;

public record EntregadorResumo(EntregadorId id, String nome, Set<String> zonasAtendidas,
        int cargaEmAndamento, int cargaMaxima, LocalDateTime ultimaEntregaEm, boolean ativo) {
}