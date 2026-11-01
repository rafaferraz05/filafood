package br.edu.cesar.filafood.app.adapter.in.web;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Tela inicial mínima com os três perfis (D-07). A guarda e a troca de perfil na sessão entram no
 * G-22; aqui só se prova que a aplicação sobe e a view carrega.
 */
@Route("")
@PageTitle("FilaFood")
public class InicioView extends VerticalLayout {

    public InicioView() {
        setPadding(true);
        setSpacing(true);
        add(
                new H1("FilaFood"),
                new Paragraph("Pedidos entre unidades de uma rede de restaurantes."),
                perfil("Gestor da unidade", "Insumos, ficha técnica, entregadores, zonas e despacho."),
                perfil("Atendente ou cozinheiro", "Fila de preparo, itens prontos e conclusão do pedido."),
                perfil("Entregador", "Retirada, entrega e falha das entregas atribuídas."));
    }

    private VerticalLayout perfil(String titulo, String descricao) {
        VerticalLayout cartao = new VerticalLayout(new H2(titulo), new Paragraph(descricao));
        cartao.setPadding(true);
        cartao.setSpacing(false);
        cartao.getStyle().set("border", "1px solid #d0d0d0").set("border-radius", "8px");
        return cartao;
    }
}
