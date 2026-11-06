package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;

/** Faixa vermelha do protótipo: selo FF, marca e as duas pills de contexto. */
public class Topbar extends Div {

    public Topbar() {
        addClassName("topbar");

        Div selo = new Div();
        selo.addClassName("marca-selo");
        selo.setText("FF");

        Div nome = new Div();
        nome.addClassName("marca-nome");
        nome.setText("FilaFood");

        Div sub = new Div();
        sub.addClassName("marca-sub");
        sub.setText("rede de restaurantes");

        Div textos = new Div();
        textos.add(nome, sub);

        Div marca = new Div();
        marca.addClassName("marca");
        marca.add(selo, textos);

        Span unidade = new Span("Unidade: " + UnidadeDeDemonstracao.NOME);
        unidade.addClassNames("pill", "pill-clara");

        Span perfil = new Span("Gestor da unidade");
        perfil.addClassNames("pill", "pill-amarela");

        Div pills = new Div();
        pills.addClassName("topbar-pills");
        pills.add(unidade, perfil);

        add(marca, pills);
    }
}
