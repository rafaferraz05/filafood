package br.edu.cesar.filafood.app.application.estoque;

import java.util.List;
import java.util.Optional;

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;

/**
 * Tela de preparo pronta para renderizar.
 *
 * `impedimento` vem preenchido quando a ficha não pode ser explodida (produto sem ficha na
 * unidade): a tela mostra a recusa da RN 2 em vez de uma tabela de insumos vazia.
 */
public record PreparoDetalhe(
        String pedidoId,
        EstadoDoPedido estado,
        int porcoes,
        List<ItemDoPreparo> itens,
        List<InsumoDoPreparo> insumos,
        Optional<String> impedimento) {
}