package br.edu.cesar.filafood.aplicacao.entrega.entrega;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.aplicacao.entrega.evento.PublicadorDeEventos;
import br.edu.cesar.filafood.aplicacao.entrega.pedido.PedidoDaEntregaRepositorioAplicacao;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.entrega.despacho.FilaDeDespacho;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorRepositorio;
import br.edu.cesar.filafood.dominio.entrega.entrega.ConsequenciasDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.DesfechoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.Entrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaId;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaRepositorio;
import br.edu.cesar.filafood.dominio.entrega.entrega.MotivoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.evento.EntregaConcluida;
import br.edu.cesar.filafood.dominio.entrega.entrega.evento.EntregaFalhou;

/** Serviço de aplicação da entrega: retirada, entrega, falha, nova tentativa e timeline. */
public class EntregaServicoAplicacao {

    private final EntregaRepositorio entregas;
    private final EntregadorRepositorio entregadores;
    private final PedidoDaEntregaRepositorioAplicacao pedidos;
    private final PublicadorDeEventos eventos;

    public EntregaServicoAplicacao(EntregaRepositorio entregas, EntregadorRepositorio entregadores,
            PedidoDaEntregaRepositorioAplicacao pedidos, PublicadorDeEventos eventos) {
        this.entregas = entregas;
        this.entregadores = entregadores;
        this.pedidos = pedidos;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public List<EntregaResumo> listarDoEntregador(String entregadorId) {
        return entregas.ativasDoEntregador(entregadorId).stream()
                .map(entrega -> new EntregaResumo(entrega.id().toString(), entrega.pedido(), endereco(entrega),
                        entrega.estado(), entrega.taxa(), timeline(entrega)))
                .toList();
    }

    @Transactional(readOnly = true)
    public EntregaResumo detalhar(String entregaId) {
        Entrega entrega = entregas.porId(new EntregaId(UUID.fromString(entregaId)))
                .orElseThrow(() -> new RegraDeNegocioException("entrega não encontrada"));
        return new EntregaResumo(entrega.id().toString(), entrega.pedido(), endereco(entrega),
                entrega.estado(), entrega.taxa(), timeline(entrega));
    }

    @Transactional
    public void registrarRetirada(String entregaId) {
        Entrega entrega = entrega(entregaId);
        entrega.registrarRetirada();
        entregas.salvar(entrega);
    }

    @Transactional
    public void registrarEntrega(String entregaId, String recebedor) {
        Entrega entrega = entrega(entregaId);
        entrega.registrarEntrega(recebedor);
        entregas.salvar(entrega);
        entregas.flush();
        if (entrega.entregador() != null) {
            entregadores.salvar(entrega.entregador());
        }
        eventos.publicar(new EntregaConcluida(entrega.id(), entrega.pedido(), entrega.recebedor()));
    }

    @Transactional
    public ResultadoDaFalha registrarFalha(String entregaId, MotivoDaFalha motivo, String observacao) {
        Entrega entrega = entrega(entregaId);
        FilaDeDespacho fila = new FilaDeDespacho();
        ConsequenciasDaFalha consequencias = new ConsequenciasDaFalha(fila);
        DesfechoDaFalha desfecho = consequencias.registrar(entrega, motivo, observacao);

        entregas.salvar(entrega);
        entregas.flush();
        if (entrega.entregador() != null) {
            entregadores.salvar(entrega.entregador());
        }

        String novaEntregaId = null;
        if (desfecho == DesfechoDaFalha.NOVA_TENTATIVA) {
            Entrega nova = fila.entregas().get(0);
            entregas.salvar(nova);
            novaEntregaId = nova.id().toString();
        }

        eventos.publicar(new EntregaFalhou(entrega.id(), entrega.pedido(), motivo, observacao, desfecho));
        return new ResultadoDaFalha(desfecho, novaEntregaId, entrega.pedido());
    }

    private Entrega entrega(String entregaId) {
        return entregas.porId(new EntregaId(UUID.fromString(entregaId)))
                .orElseThrow(() -> new RegraDeNegocioException("entrega não encontrada"));
    }

    private static List<ItemDaTimeline> timeline(Entrega entrega) {
        return entrega.transicoes().stream()
                .map(transicao -> new ItemDaTimeline(transicao.estado(), transicao.dataHora()))
                .toList();
    }

    private static String endereco(Entrega entrega) {
        String distancia = String.format(Locale.of("pt", "BR"), "%.1f", entrega.distanciaKm());
        return entrega.zona().nome() + ", a " + distancia + " km";
    }
}
