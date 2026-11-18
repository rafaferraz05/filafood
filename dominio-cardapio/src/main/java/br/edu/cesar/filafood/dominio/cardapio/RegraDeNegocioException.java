package br.edu.cesar.filafood.dominio.cardapio;

public class RegraDeNegocioException extends IllegalArgumentException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}