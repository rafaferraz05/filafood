package br.edu.cesar.filafood.aplicacao.estoque.ficha;

import java.util.List;
import java.util.Optional;

/** Resultado da tela de ficha técnica: os produtos do cardápio e a ficha do produto escolhido. */
public record FichasDaUnidade(List<ProdutoComFicha> produtos, Optional<FichaDetalhe> ficha) {
}
