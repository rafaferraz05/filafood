package br.edu.cesar.filafood.dominio.estoque.ficha;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;

/** Linha da ficha técnica: um insumo e a quantidade consumida por porção do produto. */
public record ItemDaFicha(Insumo insumo, double quantidade) {
}
