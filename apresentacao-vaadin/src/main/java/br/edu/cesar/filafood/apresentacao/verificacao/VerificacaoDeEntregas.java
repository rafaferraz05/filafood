package br.edu.cesar.filafood.apresentacao.verificacao;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.aplicacao.entrega.despacho.DespachoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.despacho.FilaDaUnidadeResumo;
import br.edu.cesar.filafood.aplicacao.entrega.entregador.EntregadorResumo;
import br.edu.cesar.filafood.aplicacao.entrega.entregador.EntregadorServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.EntregaResumo;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.EntregaServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.ResultadoDaFalha;
import br.edu.cesar.filafood.aplicacao.entrega.zona.ZonaResumo;
import br.edu.cesar.filafood.aplicacao.entrega.zona.ZonaServicoAplicacao;
import br.edu.cesar.filafood.apresentacao.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.entrega.entrega.DesfechoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.EstadoDaEntrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.MotivoDaFalha;

/**
 * Verificação de fumaça da F2 contra o banco de dev semeado: zonas, entregadores, fila, despacho,
 * retirada/entrega e falha com nova tentativa. Roda depois do runner da F1.
 */
@Component
@Profile("verificacao")
@Order(2)
public class VerificacaoDeEntregas implements CommandLineRunner {

    private final ZonaServicoAplicacao zonas;
    private final EntregadorServicoAplicacao entregadores;
    private final DespachoServicoAplicacao despacho;
    private final EntregaServicoAplicacao entregas;

    public VerificacaoDeEntregas(ZonaServicoAplicacao zonas, EntregadorServicoAplicacao entregadores,
            DespachoServicoAplicacao despacho, EntregaServicoAplicacao entregas) {
        this.zonas = zonas;
        this.entregadores = entregadores;
        this.despacho = despacho;
        this.entregas = entregas;
    }

    @Override
    public void run(String... args) {
        String unidade = UnidadeDeDemonstracao.ID;
        List<ZonaResumo> zonasDaUnidade = zonas.pesquisarResumos(unidade);
        exigir(zonasDaUnidade.size() >= 4, "esperava as 4 zonas do seed, veio " + zonasDaUnidade.size());
        List<EntregadorResumo> entregadoresDaUnidade = entregadores.pesquisarResumos(unidade);
        exigir(entregadoresDaUnidade.size() >= 4,
                "esperava os 4 entregadores do seed, veio " + entregadoresDaUnidade.size());

        FilaDaUnidadeResumo antes = despacho.painel(unidade);
        exigir(!antes.fila().isEmpty(), "esperava pedidos na fila de despacho");

        // Libera uma entrega do Bruno para ele voltar a ter carga disponível e o despacho acontecer.
        EntregadorResumo bruno = entregadoresDaUnidade.stream()
                .filter(entregador -> entregador.nome().equals("Bruno Souza"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("VERIFICACAO FALHOU: Bruno não encontrado"));
        for (EntregaResumo entrega : entregas.listarDoEntregador(bruno.id().toString())) {
            if (entrega.estado() == EstadoDaEntrega.EM_ROTA) {
                entregas.registrarEntrega(entrega.id(), "Verificação");
                break;
            }
        }

        int despachadas = despacho.despacharPendentes(unidade);
        FilaDaUnidadeResumo depois = despacho.painel(unidade);
        exigir(despachadas >= 1, "esperava ao menos um despacho depois de liberar a carga do Bruno");
        exigir(depois.fila().size() < antes.fila().size(), "a fila deveria diminuir depois do despacho");
        System.out.println("VERIFICACAO OK: " + zonasDaUnidade.size() + " zonas, "
                + entregadoresDaUnidade.size() + " entregadores, " + despachadas
                + " pedido(s) despachado(s) pela política de menor carga.");

        // Retirada e entrega de uma entrega em rota.
        for (EntregadorResumo entregador : entregadoresDaUnidade) {
            for (EntregaResumo entrega : entregas.listarDoEntregador(entregador.id().toString())) {
                if (entrega.estado() == EstadoDaEntrega.EM_ROTA) {
                    entregas.registrarEntrega(entrega.id(), "Cliente da verificação");
                    System.out.println("VERIFICACAO OK: entrega " + entrega.pedido()
                            + " concluída e carga do entregador liberada.");
                    verificarFalha(unidade, entregadoresDaUnidade);
                    return;
                }
            }
        }
        throw new IllegalStateException("VERIFICACAO FALHOU: nenhuma entrega em rota para concluir");
    }

    private void verificarFalha(String unidade, List<EntregadorResumo> entregadoresDaUnidade) {
        for (EntregadorResumo entregador : entregadoresDaUnidade) {
            for (EntregaResumo entrega : entregas.listarDoEntregador(entregador.id().toString())) {
                if (entrega.estado() == EstadoDaEntrega.DESPACHADA) {
                    entregas.registrarRetirada(entrega.id());
                    ResultadoDaFalha resultado = entregas.registrarFalha(entrega.id(),
                            MotivoDaFalha.PROBLEMA_NO_TRANSPORTE, "falha da verificação");
                    exigir(resultado.desfecho() == DesfechoDaFalha.NOVA_TENTATIVA,
                            "a primeira falha deveria gerar nova tentativa");
                    exigir(resultado.novaEntregaId() != null, "esperava a nova entrega criada");
                    System.out.println("VERIFICACAO OK: falha da entrega " + entrega.pedido()
                            + " gerou a nova tentativa " + resultado.novaEntregaId() + ".");
                    return;
                }
            }
        }
        throw new IllegalStateException("VERIFICACAO FALHOU: nenhuma entrega despachada para falhar");
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalStateException("VERIFICACAO FALHOU: " + mensagem);
        }
    }
}
