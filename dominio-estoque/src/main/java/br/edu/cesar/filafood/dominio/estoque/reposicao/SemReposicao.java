package br.edu.cesar.filafood.dominio.estoque.reposicao;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;

/**
 * Política da unidade que não monitora reposição: nada fica a repor e o ponto de pedido é zero.
 *
 * Existe para provar que a decisão de monitorar é da política, não do caso de uso nem do insumo.
 */
public final class SemReposicao implements PoliticaDeReposicao {

    @Override
    public BigDecimal pontoDePedido(Insumo insumo, LocalDate referencia) {
        return BigDecimal.ZERO.setScale(3);
    }

    @Override
    public boolean estaARepor(Insumo insumo, LocalDate referencia) {
        return false;
    }
}