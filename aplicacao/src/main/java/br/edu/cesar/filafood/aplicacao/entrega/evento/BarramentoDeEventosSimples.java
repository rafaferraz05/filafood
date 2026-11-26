package br.edu.cesar.filafood.aplicacao.entrega.evento;

import java.util.List;

import br.edu.cesar.filafood.aplicacao.entrega.evento.OuvinteDeEventosDeEntrega;
import br.edu.cesar.filafood.aplicacao.entrega.evento.PublicadorDeEventos;
import br.edu.cesar.filafood.dominio.entrega.entrega.evento.EventoDeEntrega;

/** Barramento síncrono e em memória dos eventos de entrega (Observer). */
public class BarramentoDeEventosSimples implements PublicadorDeEventos {

    private final List<OuvinteDeEventosDeEntrega> ouvintes;

    public BarramentoDeEventosSimples(List<OuvinteDeEventosDeEntrega> ouvintes) {
        this.ouvintes = List.copyOf(ouvintes);
    }

    @Override
    public void publicar(EventoDeEntrega evento) {
        ouvintes.forEach(ouvinte -> ouvinte.ao(evento));
    }
}
