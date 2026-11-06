package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * AppShell do Vaadin: o CSS dos protótipos (docs/prototipos/estilo.css) é servido como /estilo.css
 * e carregado em todas as telas; o Lumo entra só para os componentes Vaadin herdarem as cores.
 *
 * <p>Fica no adaptador de entrada para o ArchUnit garantir que Vaadin não vaza para o resto do app.
 */
@Theme(themeClass = Lumo.class)
@StyleSheet("estilo.css")
@StyleSheet("filafood-app.css")
public class AppShell implements AppShellConfigurator {
}
