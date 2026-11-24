package br.edu.cesar.filafood.dominio.compras;

/** Recusa de uma regra de negócio da F7, com a mensagem exata que os cenários BDD conferem. */
public class RegraDeNegocioException extends IllegalArgumentException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
