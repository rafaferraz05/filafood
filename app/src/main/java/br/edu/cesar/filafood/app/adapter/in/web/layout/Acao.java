package br.edu.cesar.filafood.app.adapter.in.web.layout;

import com.vaadin.flow.component.html.NativeButton;

/** Botões do protótipo: `.btn` (primário/claro) e `.link-acao` nas ações da tabela. */
public final class Acao {

    private Acao() {
    }

    public static NativeButton primario(String texto, Runnable acao) {
        return botao(texto, "btn btn-primario", acao);
    }

    public static NativeButton claro(String texto, Runnable acao) {
        return botao(texto, "btn btn-claro", acao);
    }

    public static NativeButton link(String texto, Runnable acao) {
        return botao(texto, "link-acao", acao);
    }

    /** Ação destrutiva do protótipo (`.btn-perigo`): cancelar o preparo, por exemplo. */
    public static NativeButton perigo(String texto, Runnable acao) {
        return botao(texto, "btn btn-perigo", acao);
    }

    public static NativeButton apagada(String texto, Runnable acao) {
        return botao(texto, "link-acao apagada", acao);
    }

    private static NativeButton botao(String texto, String classes, Runnable acao) {
        NativeButton botao = new NativeButton(texto, evento -> acao.run());
        for (String classe : classes.split(" ")) {
            botao.addClassName(classe);
        }
        return botao;
    }
}
