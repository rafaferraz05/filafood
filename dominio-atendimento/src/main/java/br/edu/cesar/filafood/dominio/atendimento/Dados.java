package br.edu.cesar.filafood.dominio.atendimento;

/** Validações pequenas, compartilhadas pelos dois cadastros. */
final class Dados {
    private Dados() { }

    static String obrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) throw new RegraDeNegocioException(mensagem);
        return valor.strip();
    }

    static String telefone(String valor) {
        if (valor == null || valor.isBlank()) return "";
        String digitos = valor.replaceAll("[\\s().+\\-]", "");
        if (!digitos.matches("[0-9]{10,11}"))
            throw new RegraDeNegocioException("informe telefone com DDD e 10 ou 11 algarismos");
        return digitos;
    }
}
