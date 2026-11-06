package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.dom.Element;

/** Caixa de busca do protótipo, com a lupa em SVG e input nativo. */
public class Busca extends Div {

    private final Input campo = new Input();

    public Busca(String placeholder) {
        addClassName("busca");

        campo.setType("text");

        Element svg = new Element("svg");
        svg.setAttribute("class", "lupa");
        svg.setAttribute("width", "16");
        svg.setAttribute("height", "16");
        svg.setAttribute("viewBox", "0 0 18 18");
        svg.setAttribute("fill", "none");
        svg.setAttribute("stroke", "currentColor");
        svg.setAttribute("stroke-width", "1.8");
        svg.setAttribute("stroke-linecap", "round");

        Element circulo = new Element("circle");
        circulo.setAttribute("cx", "7.6");
        circulo.setAttribute("cy", "7.6");
        circulo.setAttribute("r", "5.2");

        Element haste = new Element("line");
        haste.setAttribute("x1", "11.6");
        haste.setAttribute("y1", "11.6");
        haste.setAttribute("x2", "15.6");
        haste.setAttribute("y2", "15.6");

        svg.appendChild(circulo);
        svg.appendChild(haste);

        campo.setPlaceholder(placeholder);
        getElement().appendChild(svg);
        add(campo);
    }

    public Input campo() {
        return campo;
    }
}
