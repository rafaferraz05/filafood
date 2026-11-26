package br.edu.cesar.filafood.aplicacao.entrega.despacho;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.aplicacao.entrega.entrega.ItemDaTimeline;
import br.edu.cesar.filafood.aplicacao.entrega.evento.PublicadorDeEventos;
import br.edu.cesar.filafood.aplicacao.entrega.pedido.PedidoDaEntrega;
import br.edu.cesar.filafood.aplicacao.entrega.pedido.PedidoDaEntregaRepositorioAplicacao;
import br.edu.cesar.filafood.dominio.entrega.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.entrega.despacho.Despacho;
import br.edu.cesar.filafood.dominio.entrega.despacho.FilaDeDespacho;
import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorRepositorio;
import br.edu.cesar.filafood.dominio.entrega.entrega.Entrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaId;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaRepositorio;
import br.edu.cesar.filafood.dominio.entrega.entrega.EstadoDaEntrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.evento.EntregaDespachada;
import br.edu.cesar.filafood.dominio.entrega.zona.EscolhaDaZona;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntrega;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntregaRepositorio;

/** Serviço de aplicação do despacho: fila, escolha do entregador e criação de entregas. */
public class DespachoServicoAplicacao {

    private final EntregaRepositorio entregas;
    private final EntregadorRepositorio entregadores;
    private final ZonaDeEntregaRepositorio zonas;
    private final PedidoDaEntregaRepositorioAplicacao pedidos;
    private final Despacho despacho;
    private final EscolhaDaZona escolhaDaZona;
    private final PublicadorDeEventos eventos;

    public DespachoServicoAplicacao(EntregaRepositorio entregas, EntregadorRepositorio entregadores,
            ZonaDeEntregaRepositorio zonas, PedidoDaEntregaRepositorioAplicacao pedidos, Despacho despacho,
            EscolhaDaZona escolhaDaZona, PublicadorDeEventos eventos) {
        this.entregas = entregas;
        this.entregadores = entregadores;
        this.zonas = zonas;
        this.pedidos = pedidos;
        this.despacho = despacho;
        this.escolhaDaZona = escolhaDaZona;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public FilaDaUnidadeResumo painel(String unidadeId) {
        List<Entrega> pendentes = entregas.aguardando(unidadeId);
        List<Entregador> daUnidade = entregadores.daUnidade(unidadeId);
        List<ItemDaFilaResumo> fila = pendentes.stream()
                .map(entrega -> new ItemDaFilaResumo(entrega.id().toString(), entrega.pedido(),
                        entrega.zona().nome(), esperaEmMinutos(entrega), motivoDaFila(entrega, daUnidade)))
                .toList();
        List<EntregaEmAndamentoResumo> emAndamento = entregas.ativasDaUnidade(unidadeId).stream()
                .map(entrega -> new EntregaEmAndamentoResumo(entrega.id().toString(), entrega.pedido(),
                        entrega.entregador() == null ? "—" : entrega.entregador().nome(), entrega.estado(),
                        entrega.taxa(), endereco(entrega), timeline(entrega)))
                .toList();
        int ativos = (int) daUnidade.stream().filter(Entregador::isAtivo).count();
        int ocupados = (int) daUnidade.stream().filter(Entregador::isAtivo)
                .filter(entregador -> entregador.cargaEmAndamento() > 0).count();
        return new FilaDaUnidadeResumo(fila, emAndamento, ativos, ocupados);
    }

    @Transactional
    public int despacharPendentes(String unidadeId) {
        List<Entrega> pendentes = entregas.aguardando(unidadeId);
        List<Entregador> daUnidade = entregadores.daUnidade(unidadeId);
        FilaDeDespacho fila = new FilaDeDespacho();
        pendentes.forEach(fila::colocar);
        int despachadas = 0;
        for (Entrega entrega : pendentes) {
            if (despacho.despachar(entrega, fila, daUnidade)) {
                entregas.salvar(entrega);
                entregadores.salvar(entrega.entregador());
                publicarDespacho(entrega);
                despachadas++;
            }
        }
        return despachadas;
    }

    @Transactional
    public boolean despachar(String entregaId) {
        Entrega entrega = entregas.porId(new EntregaId(java.util.UUID.fromString(entregaId)))
                .orElseThrow(() -> new RegraDeNegocioException("entrega não encontrada"));
        FilaDeDespacho fila = new FilaDeDespacho();
        fila.colocar(entrega);
        List<Entregador> daUnidade = entregadores.daUnidade(entrega.unidadeId());
        if (!despacho.despachar(entrega, fila, daUnidade)) {
            return false;
        }
        entregas.salvar(entrega);
        entregadores.salvar(entrega.entregador());
        publicarDespacho(entrega);
        return true;
    }

    /** Cria a entrega de um pedido concluído; ponto de entrada do futuro evento da F4. */
    @Transactional
    public String criarEntregaDoPedido(String pedidoId) {
        PedidoDaEntrega pedido = pedidos.porId(pedidoId);
        if (pedido.estado() != EstadoDoPedido.CONCLUIDO) {
            throw new RegraDeNegocioException("o pedido precisa estar concluído para entrar na fila");
        }
        if (entregas.doPedido(pedidoId).isPresent()) {
            throw new RegraDeNegocioException("o pedido já tem entrega");
        }
        ZonaDeEntrega zona = escolhaDaZona.zonaDoEndereco(pedido.endereco(), zonas.daUnidade(pedido.unidadeId()));
        double distancia = zona.centro().distanciaEmKm(pedido.endereco());
        Entrega entrega = new Entrega(EntregaId.novo(), pedido.unidadeId(), pedido.id(), zona, distancia,
                pedido.valor());
        entregas.salvar(entrega);
        despachar(entrega.id().toString());
        return entrega.id().toString();
    }

    private void publicarDespacho(Entrega entrega) {
        if (entrega.entregador() != null) {
            eventos.publicar(new EntregaDespachada(entrega.id(), entrega.pedido(), entrega.entregador().id(),
                    entrega.taxa()));
        }
    }

    private static long esperaEmMinutos(Entrega entrega) {
        if (entrega.transicoes().isEmpty()) {
            return 0;
        }
        LocalDateTime desde = entrega.transicoes().get(0).dataHora();
        return Math.max(0, Duration.between(desde, LocalDateTime.now()).toMinutes());
    }

    private static String motivoDaFila(Entrega entrega, List<Entregador> entregadores) {
        String zona = entrega.zona().nome();
        List<Entregador> elegiveis = entregadores.stream()
                .filter(Entregador::isAtivo)
                .filter(entregador -> entregador.atende(zona))
                .toList();
        if (elegiveis.isEmpty()) {
            return "nenhum entregador ativo atende a zona " + zona;
        }
        if (elegiveis.stream().noneMatch(Entregador::temCargaDisponivel)) {
            return "todos os entregadores da zona estão na carga máxima";
        }
        return "aguardando o despacho automático";
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
