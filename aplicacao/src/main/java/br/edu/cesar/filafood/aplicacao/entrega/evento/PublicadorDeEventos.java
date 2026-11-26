package br.edu.cesar.filafood.aplicacao.entrega.evento;

import br.edu.cesar.filafood.dominio.entrega.entrega.evento.EventoDeEntrega;

/** Porta do Observer: o serviço publica, o barramento entrega aos ouvintes. */
public interface PublicadorDeEventos {

    void publicar(EventoDeEntrega evento);
}