package br.edu.cesar.filafood.aplicacao.entrega.zona;

import java.math.BigDecimal;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;

public record NovaZona(String unidadeId, String nome, Coordenada centro, double raioKm,
        BigDecimal valorBase, double raioGratuitoKm, BigDecimal valorPorKmExcedente,
        BigDecimal teto, BigDecimal valorMinimoDePedido) {
}