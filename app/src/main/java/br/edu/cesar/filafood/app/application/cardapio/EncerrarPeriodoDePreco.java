package br.edu.cesar.filafood.app.application.cardapio;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;
import br.edu.cesar.filafood.dominio.cardapio.ProdutosDoCardapio;
import br.edu.cesar.filafood.dominio.cardapio.RegraDeNegocioException;

@Service
public class EncerrarPeriodoDePreco {

    private final ProdutosDoCardapio produtos;

    public EncerrarPeriodoDePreco(ProdutosDoCardapio produtos) {
        this.produtos = produtos;
    }

    @Transactional
    public void executar(String produtoId, UUID periodoId, LocalDate dataFinal) {
        Produto produto = produtos.porId(ProdutoId.de(produtoId))
                .orElseThrow(() -> new RegraDeNegocioException("produto não encontrado"));
        produto.encerrarPeriodo(periodoId, dataFinal);
        produtos.salvar(produto);
    }
}