package br.edu.cesar.filafood.app.adapter.in.web.layout;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.RouterLink;

import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;

/** Faixa vermelha do protótipo: selo FF, marca, navegação entre telas e as pills de contexto. */
public class Topbar extends Div {

    private final Map<TelaDoMenu, RouterLink> links = new LinkedHashMap<>();

    public Topbar() {
        addClassName("topbar");

        Div selo = new Div();
        selo.addClassName("marca-selo");
        selo.setText("FF");

        Div nome = new Div();
        nome.addClassName("marca-nome");
        nome.setText("FilaFood");

        Div sub = new Div();
        sub.addClassName("marca-sub");
        sub.setText("rede de restaurantes");

        Div textos = new Div();
        textos.add(nome, sub);

        Div marca = new Div();
        marca.addClassName("marca");
        marca.add(selo, textos);

        Span unidade = new Span("Unidade: " + UnidadeDeDemonstracao.NOME);
        unidade.addClassNames("pill", "pill-clara");

        Span perfil = new Span("Gestor da unidade");
        perfil.addClassNames("pill", "pill-amarela");

        Div pills = new Div();
        pills.addClassName("topbar-pills");
        pills.add(unidade, perfil);

        add(marca, navegacao(), pills);
    }

    private Div navegacao() {
        Div navegacao = new Div();
        navegacao.addClassName("topbar-nav");
        for (TelaDoMenu tela : TelaDoMenu.values()) {
            RouterLink link = new RouterLink(tela.titulo(), tela.view());
            link.addClassName("topbar-nav-item");
            links.put(tela, link);
            navegacao.add(link);
        }
        return navegacao;
    }

    /** Marca na navegação a tela atual; chamado pelo layout a cada navegação. */
    public void marcarAtual(String caminho) {
        links.forEach((tela, link) -> {
            if (tela.correspondeA(caminho)) {
                link.addClassName("topbar-nav-atual");
            } else {
                link.removeClassName("topbar-nav-atual");
            }
        });
    }
}