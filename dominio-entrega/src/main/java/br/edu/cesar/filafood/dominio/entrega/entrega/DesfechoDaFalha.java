package br.edu.cesar.filafood.dominio.entrega.entrega;

/** Decide o que acontece com o pedido quando uma entrega falha. */
public enum DesfechoDaFalha {
    NOVA_TENTATIVA,
    PEDIDO_CANCELADO
}
