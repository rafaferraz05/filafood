package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.html.Input;

/**
 * Inputs do protótipo: o CSS de docs/prototipos/estilo.css seleciona `input[type="text"]`, então
 * todo campo de texto nasce com esse tipo.
 */
public final class Entradas {

    private Entradas() {
    }

    public static Input texto() {
        Input entrada = new Input();
        entrada.setType("text");
        return entrada;
    }
}
