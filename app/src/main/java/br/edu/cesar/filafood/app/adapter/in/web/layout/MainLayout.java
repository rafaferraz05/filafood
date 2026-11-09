package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;

/**
 * Casca de todas as telas: a mesma estrutura do protótipo (`.casca > .app > topbar + .conteudo`),
 * com o CSS de docs/prototipos/estilo.css carregado pelo AppShell (Application).
 */
public class MainLayout extends Div implements RouterLayout, BeforeEnterObserver {

    private final Element conteudo = new Element("main");
    private final Topbar topbar = new Topbar();

    public MainLayout() {
        addClassName("casca");

        Div app = new Div();
        app.addClassName("app");
        app.add(topbar);

        conteudo.getClassList().add("conteudo");
        app.getElement().appendChild(conteudo);
        add(app);
    }

    /** Atualiza o destaque da topbar a cada navegação (o layout é reaproveitado entre as telas). */
    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        topbar.marcarAtual(evento.getLocation().getPath());
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        conteudo.removeAllChildren();
        if (content != null) {
            conteudo.appendChild(content.getElement());
        }
    }
}
