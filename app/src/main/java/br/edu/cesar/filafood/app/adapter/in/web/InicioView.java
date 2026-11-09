package br.edu.cesar.filafood.app.adapter.in.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.app.adapter.in.web.layout.Acao;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Badge;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Cabecalho;
import br.edu.cesar.filafood.app.adapter.in.web.layout.MainLayout;

/** Início no layout e no CSS do protótipo: três perfis, com a entrada de F1 liberada. */
@Route(value = "", layout = MainLayout.class)
@PageTitle("FilaFood")
public class InicioView extends Div {

    public InicioView() {
        Cabecalho cabecalho = new Cabecalho("FilaFood",
                "Pedidos entre unidades de uma rede de restaurantes.");
        cabecalho.badges(Badge.neutro("protótipo acadêmico"));

        Div grid = new Div();
        grid.addClassName("grid-2");
        grid.add(card("Gestor da unidade", "Insumos, ficha técnica, entregadores e despacho.",
                Acao.primario("Abrir insumos", () -> getUI().ifPresent(ui -> ui.navigate("insumos"))),
                Acao.claro("Abrir ficha técnica",
                        () -> getUI().ifPresent(ui -> ui.navigate("ficha-tecnica")))));
        grid.add(card("Atendente ou cozinheiro", "Fila de preparo, itens prontos e conclusão do pedido."));
        grid.add(card("Entregador", "Retirada, entrega e falha das entregas atribuídas."));

        add(cabecalho, grid);
    }

    private Div card(String titulo, String descricao, Component... acoes) {
        Div tituloEl = new Div();
        tituloEl.addClassName("card-titulo");
        tituloEl.setText(titulo);

        Paragraph texto = new Paragraph(descricao);
        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(texto);
        if (acoes.length > 0) {
            Div linha = new Div();
            linha.addClassName("acoes-form");
            linha.add(acoes);
            corpo.add(linha);
        }

        Div card = new Div();
        card.addClassName("card");
        card.add(tituloEl, corpo);
        return card;
    }
}
