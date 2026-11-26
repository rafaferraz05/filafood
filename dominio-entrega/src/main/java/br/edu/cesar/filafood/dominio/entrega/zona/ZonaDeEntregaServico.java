package br.edu.cesar.filafood.dominio.entrega.zona;

import java.util.List;
import java.util.Optional;

import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;

/** Serviço de domínio da zona de entrega: operações de cadastro e consulta pela porta. */
public class ZonaDeEntregaServico {

    private final ZonaDeEntregaRepositorio repositorio;

    public ZonaDeEntregaServico(ZonaDeEntregaRepositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("o repositório de zonas é obrigatório");
        }
        this.repositorio = repositorio;
    }

    public void salvar(ZonaDeEntrega zona) {
        repositorio.salvar(zona);
    }

    public ZonaDeEntrega obter(ZonaId id) {
        return repositorio.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException("zona não encontrada"));
    }

    public Optional<ZonaDeEntrega> porNome(String unidadeId, String nome) {
        return repositorio.porNome(unidadeId, nome);
    }

    public List<ZonaDeEntrega> daUnidade(String unidadeId) {
        return repositorio.daUnidade(unidadeId);
    }
}
