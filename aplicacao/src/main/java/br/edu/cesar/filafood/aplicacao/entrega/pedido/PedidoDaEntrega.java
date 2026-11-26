package br.edu.cesar.filafood.aplicacao.entrega.pedido;

import java.math.BigDecimal;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.EstadoDoPedido;

public record PedidoDaEntrega(String id, String unidadeId, EstadoDoPedido estado, BigDecimal valor,
        Coordenada endereco, String enderecoTexto) {
}