package br.edu.cesar.filafood.dominio.estoque;

/**
 * Recusa de uma operação por regra de negócio. A mensagem é a que o sistema mostra ao usuário,
 * e é a mesma que os cenários BDD verificam.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
