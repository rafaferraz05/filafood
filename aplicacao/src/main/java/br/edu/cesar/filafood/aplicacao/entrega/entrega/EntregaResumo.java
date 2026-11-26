package br.edu.cesar.filafood.aplicacao.entrega.entrega;

import java.math.BigDecimal;
import java.util.List;

import br.edu.cesar.filafood.dominio.entrega.entrega.EstadoDaEntrega;

public record EntregaResumo(String id, String pedido, String endereco, EstadoDaEntrega estado,
        BigDecimal taxa, List<ItemDaTimeline> timeline) {
}