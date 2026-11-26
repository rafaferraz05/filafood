package br.edu.cesar.filafood.dominio.entrega.entregador;

import java.util.List;
import java.util.Optional;

import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;

/** Serviço de domínio do entregador: operações de cadastro e consulta pela porta. */
public class EntregadorServico {

    private final EntregadorRepositorio repositorio;

    public EntregadorServico(EntregadorRepositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("o repositório de entregadores é obrigatório");
        }
        this.repositorio = repositorio;
    }

    public void salvar(Entregador entregador) {
        repositorio.salvar(entregador);
    }

    public Entregador obter(EntregadorId id) {
        return repositorio.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException("entregador não encontrado"));
    }

    public Optional<Entregador> porNome(String unidadeId, String nome) {
        return repositorio.porNome(unidadeId, nome);
    }

    public List<Entregador> daUnidade(String unidadeId) {
        return repositorio.daUnidade(unidadeId);
    }
}
