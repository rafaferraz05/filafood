package br.edu.cesar.filafood.apresentacao.layout;

import com.vaadin.flow.component.Component;

import br.edu.cesar.filafood.apresentacao.cardapio.CardapioRota;
import br.edu.cesar.filafood.apresentacao.estoque.ficha.FichaTecnicaRota;
import br.edu.cesar.filafood.apresentacao.estoque.inicio.InicioRota;
import br.edu.cesar.filafood.apresentacao.estoque.insumo.InsumosPesquisarRota;
import br.edu.cesar.filafood.apresentacao.estoque.preparo.PreparoRota;

/**
 * Telas que aparecem na navegação da topbar, na ordem em que são exibidas.
 *
 * Nova tela com frontend entra aqui como mais uma constante: a topbar e o destaque da tela atual
 * saem daqui, sem precisar mexer no layout. A rota é o caminho da {@code @Route} da view — serve só
 * para marcar a tela atual (o destino do link vem da própria classe).
 */
public enum TelaDoMenu {

    INICIO("Início", "", InicioRota.class),
    INSUMOS("Insumos", "insumos", InsumosPesquisarRota.class),
    FICHA_TECNICA("Ficha técnica", "ficha-tecnica", FichaTecnicaRota.class),
    CARDAPIO("Cardápio", "cardapio", CardapioRota.class),
    PREPARO("Preparo do pedido", "preparo", PreparoRota.class);

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
