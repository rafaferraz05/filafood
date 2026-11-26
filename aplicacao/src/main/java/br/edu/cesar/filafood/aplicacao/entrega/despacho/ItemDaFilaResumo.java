package br.edu.cesar.filafood.aplicacao.entrega.despacho;

public record ItemDaFilaResumo(String entregaId, String pedido, String zona, long esperaMinutos,
        String motivo) {
}