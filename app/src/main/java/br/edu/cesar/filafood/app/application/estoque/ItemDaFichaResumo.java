package br.edu.cesar.filafood.app.application.estoque;

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
