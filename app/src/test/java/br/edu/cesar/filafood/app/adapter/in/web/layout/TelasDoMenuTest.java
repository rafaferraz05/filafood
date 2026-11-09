package br.edu.cesar.filafood.app.adapter.in.web.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.router.Route;

/**
 * Guarda de escalabilidade da navegação: a rota declarada em {@link TelaDoMenu} tem que ser a mesma
 * {@code @Route} da view e toda tela do menu tem que usar a casca {@code MainLayout}. Se alguém
 * renomear a rota e esquecer o menu (ou o contrário), este teste acusa.
 */
class TelasDoMenuTest {

    @Test
    void cadaTelaDoMenuApontaParaARotaDaPropriaView() {
        for (TelaDoMenu tela : TelaDoMenu.values()) {
            Route rota = tela.view().getAnnotation(Route.class);
            assertNotNull(rota, tela + " deveria ter @Route");
            assertEquals(tela.rota(), rota.value(),
                    "rota do menu diverge da @Route de " + tela.view().getSimpleName());
            assertEquals(MainLayout.class, rota.layout(), tela + " deveria usar a casca MainLayout");
        }
    }
}