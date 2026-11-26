package br.edu.cesar.filafood.aplicacao.entrega.entregador;

import java.util.Set;

public record AlteracaoDeEntregador(String entregadorId, String nome, Set<String> zonasAtendidas,
        int cargaMaxima) {
}