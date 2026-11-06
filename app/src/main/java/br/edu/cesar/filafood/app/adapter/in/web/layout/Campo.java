package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

/** Campo do protótipo: `label` em maiúsculas acima da entrada. */
public class Campo extends Div {

    public Campo(String rotulo, Component entrada) {
        addClassName("campo");

        Element label = new Element("label");
        label.setText(rotulo);

        getElement().appendChild(label);
        getElement().appendChild(entrada.getElement());
    }
}
