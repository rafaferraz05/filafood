package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.util.Optional;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/**
 * Serviço de domínio do insumo: as operações de cadastro e consulta que a aplicação usa, sempre
 * através da porta {@link InsumoRepositorio}.
 *
 * Não conhece Spring: é instanciado no composition root da apresentação.
 */
public class InsumoServico {

    private final InsumoRepositorio repositorio;

    public InsumoServico(InsumoRepositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("o repositório de insumos é obrigatório");
        }
        this.repositorio = repositorio;
    }

    public void salvar(Insumo insumo) {
        if (insumo == null) {
            throw new RegraDeNegocioException("informe o insumo");
        }
        repositorio.salvar(insumo);
    }

    public Insumo obter(InsumoId id) {
        return repositorio.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException("insumo não encontrado"));
    }

    public Optional<Insumo> porNome(String unidadeId, String nome) {
        return repositorio.porNome(unidadeId, nome);
    }
}