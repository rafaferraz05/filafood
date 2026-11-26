package br.edu.cesar.filafood.aplicacao.entrega.entrega;

import br.edu.cesar.filafood.dominio.entrega.entrega.DesfechoDaFalha;

public record ResultadoDaFalha(DesfechoDaFalha desfecho, String novaEntregaId, String pedido) {
}