package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorId;
import br.edu.cesar.filafood.dominio.entrega.entrega.Entrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaId;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaRepositorio;
import br.edu.cesar.filafood.infraestrutura.persistencia.jpa.PedidoJpa;
import br.edu.cesar.filafood.infraestrutura.persistencia.jpa.PedidoJpaRepository;

/** Implementação JPA da porta {@link EntregaRepositorio}. */
@Component
class EntregaRepositorioJpa implements EntregaRepositorio {

    private static final List<String> ATIVAS = List.of("AGUARDANDO", "DESPACHADA", "EM_ROTA");

    private final EntregaJpaRepository entregas;
    private final PedidoJpaRepository pedidos;
    private final ZonaEntregaJpaRepository zonas;
    private final EntregadorJpaRepository entregadores;
    private final EntregaJpaMapeador mapeador;

    EntregaRepositorioJpa(EntregaJpaRepository entregas, PedidoJpaRepository pedidos,
            ZonaEntregaJpaRepository zonas, EntregadorJpaRepository entregadores, EntregaJpaMapeador mapeador) {
        this.entregas = entregas;
        this.pedidos = pedidos;
        this.zonas = zonas;
        this.entregadores = entregadores;
        this.mapeador = mapeador;
    }

    @Override
    public void salvar(Entrega entrega) {
        PedidoJpa pedido = pedidos.findById(UUID.fromString(entrega.pedido()))
                .orElseThrow(() -> new RegraDeNegocioException("pedido não encontrado"));
        ZonaEntregaJpa zona = zonas.findById(entrega.zona().id().valor())
                .orElseThrow(() -> new RegraDeNegocioException("zona não encontrada"));
        EntregadorJpa entregador = entrega.entregador() == null ? null
                : entregadores.findById(entrega.entregador().id().valor())
                        .orElseThrow(() -> new RegraDeNegocioException("entregador não encontrado"));
        EntregaJpa entidade = entregas.findById(entrega.id().valor())
                .orElseGet(() -> mapeador.novaEntidade(entrega, pedido, zona, entregador));
        mapeador.atualizar(entidade, entrega, entregador);
        entregas.save(entidade);
    }

    @Override
    public void flush() {
        entregas.flush();
    }

    @Override
    public Optional<Entrega> porId(EntregaId id) {
        return entregas.findById(id.valor()).map(mapeador::paraDominio);
    }

    @Override
    public List<Entrega> aguardando(String unidadeId) {
        return entregas.findByUnidadeIdAndEstadoOrderByCriadoEmAsc(UUID.fromString(unidadeId), "AGUARDANDO")
                .stream()
                .map(mapeador::paraDominio)
                .toList();
    }

    @Override
    public List<Entrega> ativasDaUnidade(String unidadeId) {
        return entregas.findByUnidadeIdAndEstadoIn(UUID.fromString(unidadeId), ATIVAS).stream()
                .map(mapeador::paraDominio)
                .toList();
    }

    @Override
    public Optional<Entrega> doPedido(String pedido) {
        return entregas.findFirstByPedido_IdOrderByTentativaDesc(UUID.fromString(pedido))
                .map(mapeador::paraDominio);
    }

    @Override
    public List<Entrega> ativasDoEntregador(String entregadorId) {
        return entregas.findByEntregador_IdAndEstadoIn(UUID.fromString(entregadorId), ATIVAS).stream()
                .map(mapeador::paraDominio)
                .toList();
    }
}
