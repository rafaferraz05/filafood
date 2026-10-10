package br.edu.cesar.filafood.dominio.entrega;

/**
 * Estado do pedido, conforme a linguagem onipresente do projeto.
 *
 * <p>O pedido pertence ao contexto de pedido do cliente. O contexto de entrega mantém esta cópia
 * mínima porque precisa distinguir o pedido produzido do pedido cancelado. O contexto de estoque
 * tem a mesma cópia pelo mesmo motivo, e se um dia houver núcleo compartilhado entre contextos, é
 * este enum que muda de lugar.
 */
public enum EstadoDoPedido {
    RASCUNHO,
    ENVIADO,
    EM_PREPARO,
    CONCLUIDO,
    RECUSADO,
    CANCELADO
}
