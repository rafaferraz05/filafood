package br.edu.cesar.filafood.dominio.entrega.entrega;

/** Motivo da falha na entrega. É obrigatório registrar um deles. */
public enum MotivoDaFalha {
    AUSENCIA_DO_CLIENTE,
    ENDERECO_NAO_ENCONTRADO,
    RECUSA_DO_CLIENTE,
    PROBLEMA_NO_TRANSPORTE
}
