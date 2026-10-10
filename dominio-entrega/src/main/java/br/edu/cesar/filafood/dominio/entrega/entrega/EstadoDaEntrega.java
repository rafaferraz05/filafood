package br.edu.cesar.filafood.dominio.entrega.entrega;

/** Estado da entrega. A entrega percorre os estados nesta ordem, sem pular etapa. */
public enum EstadoDaEntrega {
    AGUARDANDO,
    DESPACHADA,
    EM_ROTA,
    ENTREGUE,
    FALHA
}
