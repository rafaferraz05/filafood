package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.RouterLayout;

/**
 * Casca de todas as telas: a mesma estrutura do protótipo (`.casca > .app > topbar + .conteudo`),
 * com o CSS de docs/prototipos/estilo.css carregado pelo AppShell (Application).
 */
public class MainLayout extends Div implements RouterLayout {

    private final Element conteudo = new Element("main");

    public MainLayout() {
        addClassName("casca");

        Div app = new Div();
        app.addClassName("app");
        app.add(new Topbar());

        conteudo.getClassList().add("conteudo");
        app.getElement().appendChild(conteudo);
        add(app);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        conteudo.removeAllChildren();
        if (content != null) {
            conteudo.appendChild(content.getElement());
        }
    }
}
