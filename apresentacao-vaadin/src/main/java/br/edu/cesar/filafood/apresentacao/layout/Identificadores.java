package br.edu.cesar.filafood.apresentacao.layout;

import br.edu.cesar.filafood.apresentacao.config.PedidoDeDemonstracao;

/** Formata ids que aparecem na interface, para não vazar UUID completo nas telas. */
public final class Identificadores {

    private Identificadores() {
    }

    /**
     * Número curto do pedido. O #1337 do protótipo não está no sufixo do UUID (o id termina em
     * 0501), então ele é mapeado explicitamente; os demais pedidos de demonstração usam o sufixo.
     */
    public static String pedido(String pedidoId) {
        if (pedidoId == null || pedidoId.isBlank()) {
            return "—";
        }
        if (PedidoDeDemonstracao.ID.equals(pedidoId)) {
            return "#" + PedidoDeDemonstracao.NUMERO;
        }
        String digitos = pedidoId.replaceAll("[^0-9]", "");
        if (digitos.length() >= 4) {
            return "#" + digitos.substring(digitos.length() - 4);
        }
        return "#" + pedidoId;
    }

    /** Origem de uma movimentação: pedido vira "Pedido #1341"; texto livre fica como está. */
    public static String origem(String origem) {
        if (origem == null || origem.isBlank()) {
            return "—";
        }
        if (origem.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            return "Pedido " + pedido(origem);
        }
        return origem;
    }
}
