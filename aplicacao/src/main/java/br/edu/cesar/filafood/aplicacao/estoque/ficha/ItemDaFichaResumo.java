package br.edu.cesar.filafood.aplicacao.estoque.ficha;

import java.math.BigDecimal;

/** Linha da ficha técnica pronta para a tela: porção, saldo e porções cobertas por insumo. */
public record ItemDaFichaResumo(
        String insumoId,
        String insumo,
        String unidadeDeMedida,
        BigDecimal porcao,
        BigDecimal saldo,
        int porcoesCobertas) {
}
