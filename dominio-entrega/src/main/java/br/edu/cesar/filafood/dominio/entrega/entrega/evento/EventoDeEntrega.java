package br.edu.cesar.filafood.dominio.entrega.entrega.evento;

/** Evento de domínio publicado quando uma entrega muda de forma relevante. */
public sealed interface EventoDeEntrega
        permits EntregaDespachada, EntregaConcluida, EntregaFalhou {

    String pedido();
}