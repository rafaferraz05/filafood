package br.edu.cesar.filafood.aplicacao.entrega.zona;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.entrega.zona.PoliticaDeTaxa;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntrega;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntregaServico;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaId;

/** Serviço de aplicação da zona: cadastro, estado e exemplos de taxa calculados pela política. */
public class ZonaServicoAplicacao {

    private final ZonaDeEntregaServico zonas;
    private final PoliticaDeTaxa politicaDeTaxa;

    public ZonaServicoAplicacao(ZonaDeEntregaServico zonas, PoliticaDeTaxa politicaDeTaxa) {
        this.zonas = zonas;
        this.politicaDeTaxa = politicaDeTaxa;
    }

    @Transactional(readOnly = true)
    public List<ZonaResumo> pesquisarResumos(String unidadeId) {
        return zonas.daUnidade(unidadeId).stream()
                .map(this::resumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public ZonaDetalhe obterDetalhe(String zonaId) {
        ZonaDeEntrega zona = zonas.obter(new ZonaId(UUID.fromString(zonaId)));
        return new ZonaDetalhe(zona.id(), zona.unidadeId(), zona.nome(), zona.centro(), zona.raioKm(),
                zona.valorBase(), zona.raioGratuitoKm(), zona.valorPorKmExcedente(), zona.teto(),
                zona.valorMinimoDePedido(), zona.isAtiva());
    }

    @Transactional
    public ZonaId cadastrar(NovaZona comando) {
        zonas.porNome(comando.unidadeId(), comando.nome()).ifPresent(outra -> {
            throw new RegraDeNegocioException("já existe uma zona com esse nome na unidade");
        });
        ZonaDeEntrega zona = new ZonaDeEntrega(ZonaId.novo(), comando.unidadeId(), comando.nome(),
                comando.centro(), comando.raioKm(), comando.valorBase(), comando.raioGratuitoKm(),
                comando.valorPorKmExcedente(), comando.teto(), comando.valorMinimoDePedido());
        zonas.salvar(zona);
        return zona.id();
    }

    @Transactional
    public void alterar(AlteracaoDeZona comando) {
        ZonaDeEntrega zona = zonas.obter(new ZonaId(UUID.fromString(comando.zonaId())));
        zonas.porNome(zona.unidadeId(), comando.nome())
                .filter(outra -> !outra.id().equals(zona.id()))
                .ifPresent(outra -> {
                    throw new RegraDeNegocioException("já existe uma zona com esse nome na unidade");
                });
        zona.alterarDados(comando.nome(), comando.centro(), comando.raioKm(), comando.valorBase(),
                comando.raioGratuitoKm(), comando.valorPorKmExcedente(), comando.teto(),
                comando.valorMinimoDePedido());
        zonas.salvar(zona);
    }

    @Transactional
    public void desativar(String zonaId) {
        ZonaDeEntrega zona = zonas.obter(new ZonaId(UUID.fromString(zonaId)));
        zona.desativar();
        zonas.salvar(zona);
    }

    @Transactional
    public void reativar(String zonaId) {
        ZonaDeEntrega zona = zonas.obter(new ZonaId(UUID.fromString(zonaId)));
        zona.reativar();
        zonas.salvar(zona);
    }

    /** Os cinco exemplos da tela de zonas, calculados pela mesma política usada no despacho. */
    @Transactional(readOnly = true)
    public List<ExemploDeTaxa> exemplosDeTaxa(String unidadeId) {
        List<ZonaDeEntrega> daUnidade = zonas.daUnidade(unidadeId);
        List<ExemploDeTaxa> exemplos = new ArrayList<>();
        ZonaDeEntrega centro = porNome(daUnidade, "Centro");
        ZonaDeEntrega orla = porNome(daUnidade, "Orla");
        if (centro != null) {
            exemplos.add(exemplo(centro, 1.5, "25,00", 25.00,
                    "25,00 a 1,5 km do Centro — dentro do raio grátis: só o valor base"));
            exemplos.add(exemplo(centro, 4.0, "25,00", 25.00,
                    "25,00 a 4 km do Centro — base + 2 km excedentes"));
            exemplos.add(exemplo(centro, 4.0, "35,00", 35.00,
                    "35,00 a 4 km do Centro — passou do mínimo: taxa zerada"));
            exemplos.add(exemplo(centro, 2.0, "25,00", 25.00,
                    "25,00 a 2 km — vale a zona de menor raio: Centro"));
        }
        if (orla != null) {
            exemplos.add(exemplo(orla, 8.0, "25,00", 25.00,
                    "25,00 a 8 km da Orla — o teto limita o valor"));
        }
        return exemplos;
    }

    private ExemploDeTaxa exemplo(ZonaDeEntrega zona, double distanciaKm, String valorTexto, double valor,
            String descricao) {
        return new ExemploDeTaxa(descricao,
                politicaDeTaxa.calcular(zona, distanciaKm, BigDecimal.valueOf(valor)));
    }

    private static ZonaDeEntrega porNome(List<ZonaDeEntrega> zonas, String nome) {
        return zonas.stream().filter(zona -> zona.nome().equals(nome)).findFirst().orElse(null);
    }

    private ZonaResumo resumo(ZonaDeEntrega zona) {
        return new ZonaResumo(zona.id(), zona.nome(), zona.centro(), zona.raioKm(), zona.valorBase(),
                zona.raioGratuitoKm(), zona.valorPorKmExcedente(), zona.teto(), zona.valorMinimoDePedido(),
                zona.isAtiva());
    }
}
