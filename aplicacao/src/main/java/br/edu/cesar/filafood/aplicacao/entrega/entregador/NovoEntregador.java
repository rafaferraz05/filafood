package br.edu.cesar.filafood.aplicacao.entrega.entregador;

import java.util.Set;

public record NovoEntregador(String unidadeId, String nome, Set<String> zonasAtendidas, int cargaMaxima) {
}