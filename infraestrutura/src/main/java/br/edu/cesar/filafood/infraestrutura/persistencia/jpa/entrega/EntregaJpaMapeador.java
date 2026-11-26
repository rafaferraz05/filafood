package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorId;
import br.edu.cesar.filafood.dominio.entrega.entrega.Entrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.EntregaId;
import br.edu.cesar.filafood.dominio.entrega.entrega.EstadoDaEntrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.MotivoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.TransicaoDaEntrega;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntrega;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaId;
import br.edu.cesar.filafood.infraestrutura.persistencia.jpa.PedidoJpa;

/** Converte os agregados de entrega entre domínio e JPA, nos dois sentidos. */
@Component
class EntregaJpaMapeador {

    // ----- Entregador ---------------------------------------------------------------------

    EntregadorJpa novaEntidade(Entregador entregador) {
        return new EntregadorJpa(entregador.id().valor(), java.util.UUID.fromString(entregador.unidadeId()),
                entregador.nome(), entregador.cargaMaxima(), entregador.cargaEmAndamento(),
                entregador.isAtivo() ? "ATIVO" : "INATIVO", instante(entregador.ultimaEntregaEm()),
                entregador.zonasAtendidas());
    }

    void atualizar(EntregadorJpa entidade, Entregador entregador) {
        entidade.atualizar(entregador.nome(), entregador.cargaMaxima(), entregador.cargaEmAndamento(),
                entregador.isAtivo() ? "ATIVO" : "INATIVO", instante(entregador.ultimaEntregaEm()),
                entregador.zonasAtendidas());
    }

    Entregador paraDominio(EntregadorJpa entidade) {
        return Entregador.reconstituir(new EntregadorId(entidade.id()), entidade.unidadeId().toString(),
                entidade.nome(), new LinkedHashSet<>(entidade.zonasAtendidas()), entidade.cargaMaxima(),
                "ATIVO".equals(entidade.estado()), entidade.cargaEmAndamento(),
                dataHora(entidade.ultimaEntregaEm()));
    }

    // ----- Zona ---------------------------------------------------------------------------

    ZonaEntregaJpa novaEntidade(ZonaDeEntrega zona) {
        return new ZonaEntregaJpa(zona.id().valor(), java.util.UUID.fromString(zona.unidadeId()), zona.nome(),
                BigDecimal.valueOf(zona.centro().latitude()), BigDecimal.valueOf(zona.centro().longitude()),
                BigDecimal.valueOf(zona.raioKm()), zona.valorBase(), BigDecimal.valueOf(zona.raioGratuitoKm()),
                zona.valorPorKmExcedente(), zona.teto(), zona.valorMinimoDePedido(),
                zona.isAtiva() ? "ATIVA" : "INATIVA");
    }

    void atualizar(ZonaEntregaJpa entidade, ZonaDeEntrega zona) {
        entidade.atualizar(zona.nome(), BigDecimal.valueOf(zona.centro().latitude()),
                BigDecimal.valueOf(zona.centro().longitude()), BigDecimal.valueOf(zona.raioKm()),
                zona.valorBase(), BigDecimal.valueOf(zona.raioGratuitoKm()), zona.valorPorKmExcedente(),
                zona.teto(), zona.valorMinimoDePedido(), zona.isAtiva() ? "ATIVA" : "INATIVA");
    }

    ZonaDeEntrega paraDominio(ZonaEntregaJpa entidade) {
        return ZonaDeEntrega.reconstituir(new ZonaId(entidade.id()), entidade.unidadeId().toString(),
                entidade.nome(), new Coordenada(entidade.centroLatitude().doubleValue(),
                        entidade.centroLongitude().doubleValue()),
                entidade.raioKm().doubleValue(), entidade.valorBase(), entidade.raioGratuitoKm().doubleValue(),
                entidade.valorPorKmExcedente(), entidade.teto(), entidade.valorMinimoPedido(),
                "ATIVA".equals(entidade.estado()));
    }

    // ----- Entrega ------------------------------------------------------------------------

    EntregaJpa novaEntidade(Entrega entrega, PedidoJpa pedido, ZonaEntregaJpa zona, EntregadorJpa entregador) {
        EntregaJpa entidade = new EntregaJpa(entrega.id().valor(),
                java.util.UUID.fromString(entrega.unidadeId()), pedido, zona,
                BigDecimal.valueOf(entrega.distanciaKm()), entrega.valorDoPedido(), entrega.tentativa(),
                entrega.estado().name(), entrega.taxa(), entregador, entrega.recebedor(),
                entrega.motivoDaFalha() == null ? null : entrega.motivoDaFalha().name(),
                entrega.observacaoDaFalha());
        preencherTransicoes(entidade, entrega);
        return entidade;
    }

    void atualizar(EntregaJpa entidade, Entrega entrega, EntregadorJpa entregador) {
        entidade.atualizar(entrega.estado().name(), entrega.taxa(), entregador, entrega.recebedor(),
                entrega.motivoDaFalha() == null ? null : entrega.motivoDaFalha().name(),
                entrega.observacaoDaFalha());
        int existentes = entidade.transicoes().size();
        int ordem = existentes + 1;
        for (int i = existentes; i < entrega.transicoes().size(); i++) {
            TransicaoDaEntrega transicao = entrega.transicoes().get(i);
            entidade.adicionarTransicao(new TransicaoEntregaJpa(entidade, ordem++, transicao.estado().name(),
                    instante(transicao.dataHora())));
        }
    }

    private void preencherTransicoes(EntregaJpa entidade, Entrega entrega) {
        int ordem = 1;
        for (TransicaoDaEntrega transicao : entrega.transicoes()) {
            entidade.adicionarTransicao(new TransicaoEntregaJpa(entidade, ordem++, transicao.estado().name(),
                    instante(transicao.dataHora())));
        }
    }

    Entrega paraDominio(EntregaJpa entidade) {
        List<TransicaoDaEntrega> transicoes = entidade.transicoes().stream()
                .map(transicao -> new TransicaoDaEntrega(EstadoDaEntrega.valueOf(transicao.estado()),
                        dataHora(transicao.dataHora())))
                .toList();
        return Entrega.reconstituir(new EntregaId(entidade.id()), entidade.unidadeId().toString(),
                entidade.pedido().id().toString(), paraDominio(entidade.zona()),
                entidade.distanciaKm().doubleValue(), entidade.valorPedido(), entidade.tentativa(),
                EstadoDaEntrega.valueOf(entidade.estado()),
                entidade.entregador() == null ? null : paraDominio(entidade.entregador()),
                entidade.taxa(), entidade.recebedor(),
                entidade.motivoFalha() == null ? null : MotivoDaFalha.valueOf(entidade.motivoFalha()),
                entidade.observacaoFalha(), transicoes);
    }

    private static Instant instante(LocalDateTime dataHora) {
        return dataHora == null ? null : dataHora.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static LocalDateTime dataHora(Instant instante) {
        return instante == null ? null : LocalDateTime.ofInstant(instante, ZoneId.systemDefault());
    }

    @SuppressWarnings("unused")
    private static Set<String> copia(Set<String> zonas) {
        return new LinkedHashSet<>(zonas);
    }
}
