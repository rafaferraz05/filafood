package br.edu.cesar.filafood.dominio.entrega.suporte;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.entrega.despacho.Despacho;
import br.edu.cesar.filafood.dominio.entrega.despacho.FilaDeDespacho;
import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.entrega.ConsequenciasDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.Entrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.MotivoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntrega;

/**
 * Estado compartilhado pelos passos de um cenário, injetado pelo construtor das classes de passos.
 *
 * <p>Sem ele, cada classe de passos teria a própria fila e os próprios entregadores, e um cenário
 * que atravessa duas classes veria dois mundos diferentes.
 */
public class MundoDaEntrega {

    /** Centro usado pelas zonas padrão dos cenários. */
    public static final Coordenada CENTRO_PADRAO = new Coordenada(-8.0533, -34.8813);

    public final ZonaDeEntrega.ZonasDaUnidade zonas = new ZonaDeEntrega.ZonasDaUnidade();
    public final FilaDeDespacho fila = new FilaDeDespacho();
    public final Despacho despacho = new Despacho();
    public final ConsequenciasDaFalha consequencias = new ConsequenciasDaFalha(fila);
    public final List<Entregador> entregadores = new ArrayList<>();

    public PedidoEmTeste pedido;
    public Entrega entrega;
    public RegraDeNegocioException recusa;

    public void tentar(Runnable acao) {
        try {
            acao.run();
            recusa = null;
        } catch (RegraDeNegocioException e) {
            recusa = e;
        }
    }

    public void semRecusa() {
        if (recusa != null) {
            throw new AssertionError("o sistema recusou a operação: " + recusa.getMessage());
        }
    }

    /** Coordenada a uma distância exata, em quilômetros, ao norte do centro informado. */
    public static Coordenada aoNorte(Coordenada centro, double km) {
        double grausPorKm = 180 / (Math.PI * Coordenada.RAIO_DA_TERRA_KM);
        return new Coordenada(centro.latitude() + km * grausPorKm, centro.longitude());
    }

    /** Cadastra a zona se ela ainda não existir, com o centro padrão dos cenários. */
    public ZonaDeEntrega garantirZona(String nome, double raioKm, double valorBase, double raioGratuitoKm,
            double valorPorKmExcedente, double teto, double valorMinimoDePedido) {
        if (zonas.zonas().stream().anyMatch(zona -> zona.nome().equals(nome))) {
            return zona(nome);
        }
        return zonas.cadastrar(new ZonaDeEntrega(nome, CENTRO_PADRAO, raioKm, valorBase, raioGratuitoKm,
                valorPorKmExcedente, teto, valorMinimoDePedido));
    }

    public ZonaDeEntrega zona(String nome) {
        return zonas.zonas().stream()
                .filter(zona -> zona.nome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("zona não cadastrada no cenário: " + nome));
    }

    public Entregador cadastrarEntregador(String nome, Set<String> zonasAtendidas, int cargaMaxima) {
        Entregador entregador = new Entregador(nome, zonasAtendidas, cargaMaxima);
        entregadores.add(entregador);
        return entregador;
    }

    public Entregador cadastrarEntregador(String nome, String zona, int cargaMaxima) {
        return cadastrarEntregador(nome, new LinkedHashSet<>(Set.of(zona)), cargaMaxima);
    }

    public Entregador entregador(String nome) {
        return entregadores.stream()
                .filter(entregador -> entregador.nome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("entregador não cadastrado no cenário: " + nome));
    }

    /** Cadastra o entregador se ele ainda não existir no cenário. */
    public Entregador garantirEntregador(String nome, String zona, int cargaMaxima) {
        return entregadores.stream()
                .filter(entregador -> entregador.nome().equals(nome))
                .findFirst()
                .orElseGet(() -> cadastrarEntregador(nome, zona, cargaMaxima));
    }

    /** Sobe a carga do entregador com o número de entregas informado. */
    public static void assumir(int quantidade, Entregador entregador) {
        for (int i = 0; i < quantidade; i++) {
            entregador.assumirEntrega(LocalDateTime.now());
        }
    }

    /** Traduz o motivo escrito no cenário para o motivo do domínio. */
    public static MotivoDaFalha motivo(String texto) {
        return switch (texto) {
            case "ausência do cliente" -> MotivoDaFalha.AUSENCIA_DO_CLIENTE;
            case "endereço não encontrado" -> MotivoDaFalha.ENDERECO_NAO_ENCONTRADO;
            case "recusa do cliente" -> MotivoDaFalha.RECUSA_DO_CLIENTE;
            case "problema no transporte" -> MotivoDaFalha.PROBLEMA_NO_TRANSPORTE;
            default -> throw new IllegalArgumentException("motivo não previsto: " + texto);
        };
    }

    /** Pedido pronto, com a entrega criada em aguardando e colocada na fila de despacho. */
    public Entrega pedidoPronto(String id, double valorDoPedido, Coordenada endereco) {
        pedido = new PedidoEmTeste(id, valorDoPedido, endereco);
        pedido.marcarConcluido();
        ZonaDeEntrega zona = zonas.zonaDoEndereco(endereco);
        double distancia = zona.centro().distanciaEmKm(endereco);
        entrega = new Entrega(id, zona, distancia, valorDoPedido);
        fila.colocar(entrega);
        return entrega;
    }

    public boolean despacharPedido() {
        return despacho.despachar(entrega, fila, entregadores);
    }

    public EstadoDoPedido estadoDoPedido() {
        return pedido.estado();
    }
}
