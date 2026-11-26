package br.edu.cesar.filafood.dominio.entrega.entrega.evento;

import br.edu.cesar.filafood.dominio.entrega.entrega.DesfechoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaId;
import br.edu.cesar.filafood.dominio.entrega.entrega.MotivoDaFalha;

public record EntregaFalhou(EntregaId entrega, String pedido, MotivoDaFalha motivo, String observacao,
        DesfechoDaFalha desfecho) implements EventoDeEntrega {
}