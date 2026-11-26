package br.edu.cesar.filafood.aplicacao.entrega.despacho;

import java.util.List;

public record FilaDaUnidadeResumo(List<ItemDaFilaResumo> fila,
        List<EntregaEmAndamentoResumo> emAndamento, int entregadoresAtivos, int entregadoresOcupados) {
}