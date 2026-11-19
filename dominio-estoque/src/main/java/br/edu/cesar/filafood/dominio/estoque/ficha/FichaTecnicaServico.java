package br.edu.cesar.filafood.dominio.estoque.ficha;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/**
 * Serviço de domínio da ficha técnica: salvar, obter e remover a ficha de um produto na unidade,
 * sempre através da porta {@link FichaTecnicaRepositorio}.
 *
 * Não conhece Spring: é instanciado no composition root da apresentação.
 */
public class FichaTecnicaServico {

    private final FichaTecnicaRepositorio repositorio;

    public FichaTecnicaServico(FichaTecnicaRepositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("o repositório de fichas técnicas é obrigatório");
        }
        this.repositorio = repositorio;
    }

    public void salvar(FichaTecnica ficha) {
        if (ficha == null) {
            throw new RegraDeNegocioException("informe a ficha técnica");
        }
        repositorio.salvar(ficha);
    }

    /** Abre a ficha do produto para edição, criando-a se ainda não existir. */
    public FichaTecnica abrir(String unidadeId, ProdutoId produto) {
        return repositorio.porProduto(unidadeId, produto)
                .orElseGet(() -> new FichaTecnica(FichaTecnicaId.novo(), unidadeId, produto));
    }

    public FichaTecnica obter(String unidadeId, ProdutoId produto) {
        return repositorio.porProduto(unidadeId, produto)
                .orElseThrow(() -> new RegraDeNegocioException("produto sem ficha técnica na unidade"));
    }

    public void remover(FichaTecnica ficha) {
        repositorio.remover(ficha);
    }
}