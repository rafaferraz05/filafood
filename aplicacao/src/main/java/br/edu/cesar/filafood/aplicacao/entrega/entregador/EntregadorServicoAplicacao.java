package br.edu.cesar.filafood.aplicacao.entrega.entregador;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorId;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorServico;

/** Serviço de aplicação do entregador: leitura para a tela e escrita coordenada pela RN 1. */
public class EntregadorServicoAplicacao {

    private final EntregadorServico entregadores;

    public EntregadorServicoAplicacao(EntregadorServico entregadores) {
        this.entregadores = entregadores;
    }

    @Transactional(readOnly = true)
    public List<EntregadorResumo> pesquisarResumos(String unidadeId) {
        return entregadores.daUnidade(unidadeId).stream()
                .map(entregador -> new EntregadorResumo(entregador.id(), entregador.nome(),
                        entregador.zonasAtendidas(), entregador.cargaEmAndamento(), entregador.cargaMaxima(),
                        entregador.ultimaEntregaEm(), entregador.isAtivo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public EntregadorDetalhe obterDetalhe(String entregadorId) {
        Entregador entregador = entregadores.obter(new EntregadorId(UUID.fromString(entregadorId)));
        return new EntregadorDetalhe(entregador.id(), entregador.unidadeId(), entregador.nome(),
                entregador.zonasAtendidas(), entregador.cargaMaxima(), entregador.isAtivo());
    }

    @Transactional
    public EntregadorId cadastrar(NovoEntregador comando) {
        Entregador entregador = new Entregador(EntregadorId.novo(), comando.unidadeId(), comando.nome(),
                comando.zonasAtendidas(), comando.cargaMaxima());
        entregadores.salvar(entregador);
        return entregador.id();
    }

    @Transactional
    public void alterar(AlteracaoDeEntregador comando) {
        Entregador entregador = entregadores.obter(new EntregadorId(UUID.fromString(comando.entregadorId())));
        entregador.alterarDados(comando.nome(), comando.zonasAtendidas(), comando.cargaMaxima());
        entregadores.salvar(entregador);
    }

    @Transactional
    public void desativar(String entregadorId) {
        Entregador entregador = entregadores.obter(new EntregadorId(UUID.fromString(entregadorId)));
        entregador.desativar();
        entregadores.salvar(entregador);
    }

    @Transactional
    public void reativar(String entregadorId) {
        Entregador entregador = entregadores.obter(new EntregadorId(UUID.fromString(entregadorId)));
        entregador.reativar();
        entregadores.salvar(entregador);
    }
}
