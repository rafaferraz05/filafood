package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.html.Span;

/** Badges do protótipo (`.badge`, `.badge-ok`, `.badge-repor`, `.badge-neutro`). */
public final class Badge {

    private Badge() {
    }

    public static Span neutro(String texto) {
        return criar("badge-neutro", texto, false);
    }

    public static Span ok(String texto) {
        return criar("badge-ok", texto, true);
    }

    public static Span repor(String texto) {
        return criar("badge-repor", texto, true);
    }

    private static Span criar(String variante, String texto, boolean comBolinha) {
        Span badge = new Span();
        badge.addClassNames("badge", variante);
        if (comBolinha) {
            Span bolinha = new Span();
            bolinha.addClassName("bolinha");
            badge.add(bolinha);
        }
        badge.add(new Span(texto));
        return badge;
    }
}
