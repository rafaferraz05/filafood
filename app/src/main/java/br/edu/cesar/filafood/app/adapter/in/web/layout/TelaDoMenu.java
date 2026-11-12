package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.Component;

import br.edu.cesar.filafood.app.adapter.in.web.FichaTecnicaView;
import br.edu.cesar.filafood.app.adapter.in.web.InicioView;
import br.edu.cesar.filafood.app.adapter.in.web.InsumosView;
import br.edu.cesar.filafood.app.adapter.in.web.PreparoView;

/**
 * Telas que aparecem na navegação da topbar, na ordem em que são exibidas.
 *
 * Nova tela com frontend entra aqui como mais uma constante: a topbar e o destaque da tela atual
 * saem daqui, sem precisar mexer no layout. A rota é o caminho da {@code @Route} da view — serve só
 * para marcar a tela atual (o destino do link vem da própria classe).
 */
public enum TelaDoMenu {

    INICIO("Início", "", InicioView.class),
    INSUMOS("Insumos", "insumos", InsumosView.class),
    FICHA_TECNICA("Ficha técnica", "ficha-tecnica", FichaTecnicaView.class),
    PREPARO("Preparo do pedido", "preparo", PreparoView.class);

    private final String titulo;
    private final String rota;
    private final Class<? extends Component> view;

    TelaDoMenu(String titulo, String rota, Class<? extends Component> view) {
        this.titulo = titulo;
        this.rota = rota;
        this.view = view;
    }

    public String titulo() {
        return titulo;
    }

    public String rota() {
        return rota;
    }

    public Class<? extends Component> view() {
        return view;
    }

    /** A tela atual é esta quando o caminho é exatamente a rota ou uma tela filha dela. */
    public boolean correspondeA(String caminho) {
        return rota.isEmpty() ? caminho.isEmpty() : caminho.equals(rota) || caminho.startsWith(rota + "/");
    }
}
