package br.edu.cesar.filafood.aplicacao.entrega.entregador;

import java.util.Set;

import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorId;

public record EntregadorDetalhe(EntregadorId id, String unidadeId, String nome,
        Set<String> zonasAtendidas, int cargaMaxima, boolean ativo) {
}