package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;

/** Cabeçalho de tela do protótipo: h1 com sublinhado, subtítulo e badges à direita. */
public class Cabecalho extends Div {

    private final Div badges = new Div();

    public Cabecalho(String titulo, String subtitulo) {
        addClassName("cabecalho");

        Paragraph texto = new Paragraph(subtitulo);
        texto.addClassName("subtitulo");

        Div textos = new Div();
        textos.add(new H1(titulo), texto);

        badges.addClassName("badges");
        add(textos, badges);
    }

    /** Troca os badges (contagens e estado mudam a cada renderização). */
    public Cabecalho badges(Component... componentes) {
        badges.removeAll();
        badges.add(componentes);
        return this;
    }
}
