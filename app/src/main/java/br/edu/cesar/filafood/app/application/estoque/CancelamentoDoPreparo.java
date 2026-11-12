package br.edu.cesar.filafood.app.application.estoque;

/** Comando de cancelamento do preparo: devolve ao estoque o que a baixa consumiu (F1 RN 5). */
public record CancelamentoDoPreparo(String pedidoId) {
}