package br.edu.cesar.filafood.aplicacao.entrega.despacho;

import java.math.BigDecimal;
import java.util.List;

import br.edu.cesar.filafood.aplicacao.entrega.entrega.ItemDaTimeline;
import br.edu.cesar.filafood.dominio.entrega.entrega.EstadoDaEntrega;

public record EntregaEmAndamentoResumo(String entregaId, String pedido, String entregador,
        EstadoDaEntrega estado, BigDecimal taxa, String endereco, List<ItemDaTimeline> timeline) {
}