package br.edu.cesar.filafood.aplicacao.entrega.evento;

import br.edu.cesar.filafood.dominio.entrega.entrega.evento.EventoDeEntrega;

/** Observador dos eventos de entrega. */
public interface OuvinteDeEventosDeEntrega {

    void ao(EventoDeEntrega evento);
}