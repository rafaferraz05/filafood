package br.edu.cesar.filafood.dominio.estoque;

/**
 * Estado do pedido, conforme a linguagem onipresente do projeto.
 *
 * O pedido pertence ao contexto de pedido do cliente. O contexto de estoque mantém esta cópia
 * mínima porque a devolução do consumo só é aceita para pedido em preparo. Se um dia houver um
 * núcleo compartilhado entre contextos, é este enum que muda de lugar.
 */
public enum EstadoDoPedido {
    RASCUNHO,
    ENVIADO,
    EM_PREPARO,
    CONCLUIDO,
    RECUSADO,
    CANCELADO
}
