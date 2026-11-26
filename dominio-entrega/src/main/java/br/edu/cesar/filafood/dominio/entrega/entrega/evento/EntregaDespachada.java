package br.edu.cesar.filafood.dominio.entrega.entrega.evento;

import java.math.BigDecimal;

import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorId;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaId;

public record EntregaDespachada(EntregaId entrega, String pedido, EntregadorId entregador, BigDecimal taxa)
        implements EventoDeEntrega {
}