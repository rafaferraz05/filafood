package br.edu.cesar.filafood.dominio.entrega;

/**
 * Recusa de uma operação por regra de negócio. A mensagem é a que o sistema mostra ao usuário,
 * e é a mesma que os cenários BDD verificam.
 *
 * <p>Cada contexto limitado tem a sua: o estoque tem a dele, a entrega tem esta. Compartilhar
 * exceção entre contextos criaria acoplamento por um detalhe que não é do domínio.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
