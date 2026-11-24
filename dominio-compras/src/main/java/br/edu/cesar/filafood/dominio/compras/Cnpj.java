package br.edu.cesar.filafood.dominio.compras;

/**
 * CNPJ do fornecedor (F7 RN 1): 14 algarismos com os dois dígitos verificadores válidos.
 *
 * <p>Aceita a forma pontuada (11.222.333/0001-81) e guarda só os algarismos, que é a forma usada
 * para a unicidade na rede.
 */
public record Cnpj(String valor) {

    private static final int[] PESOS_DO_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_DO_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cnpj {
        String algarismos = valor == null ? "" : valor.replaceAll("[.\\-/\\s]", "");
        if (!algarismos.matches("\\d{14}")) {
            throw new RegraDeNegocioException("o CNPJ deve ter 14 algarismos");
        }
        if (algarismos.chars().distinct().count() == 1 || !digitosConferem(algarismos)) {
            throw new RegraDeNegocioException("CNPJ inválido");
        }
        valor = algarismos;
    }

    public static Cnpj de(String texto) {
        return new Cnpj(texto);
    }

    /** Forma de exibição: 11.222.333/0001-81. */
    public String formatado() {
        return valor.substring(0, 2) + "." + valor.substring(2, 5) + "." + valor.substring(5, 8) + "/"
                + valor.substring(8, 12) + "-" + valor.substring(12);
    }

    private static boolean digitosConferem(String algarismos) {
        int primeiro = digito(algarismos, PESOS_DO_PRIMEIRO);
        int segundo = digito(algarismos, PESOS_DO_SEGUNDO);
        return primeiro == algarismos.charAt(12) - '0' && segundo == algarismos.charAt(13) - '0';
    }

    private static int digito(String algarismos, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (algarismos.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    @Override
    public String toString() {
        return valor;
    }
}
