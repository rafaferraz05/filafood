package br.edu.cesar.filafood.dominio.entrega.entrega.evento;

import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaId;

public record EntregaConcluida(EntregaId entrega, String pedido, String recebedor)
        implements EventoDeEntrega {
}