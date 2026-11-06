package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

/**
 * Tabela do protótipo: constrói o mesmo `table/thead/tbody` que o CSS estiliza, em vez de usar o
 * `vaadin-grid` (shadow DOM) — assim as regras de `estilo.css` valem sem reescrita.
 */
public class Tabela extends Div {

    private final Element corpo = new Element("tbody");

    public Tabela(String... colunas) {
        Element linha = new Element("tr");
        for (String coluna : colunas) {
            Element th = new Element("th");
            th.setText(coluna);
            linha.appendChild(th);
        }

        Element thead = new Element("thead");
        thead.appendChild(linha);

        Element tabela = new Element("table");
        tabela.appendChild(thead);
        tabela.appendChild(corpo);
        getElement().appendChild(tabela);
    }

    public Tabela linha(Component... celulas) {
        Element tr = new Element("tr");
        for (Component celula : celulas) {
            Element td = new Element("td");
            td.appendChild(celula.getElement());
            tr.appendChild(td);
        }
        corpo.appendChild(tr);
        return this;
    }
}
