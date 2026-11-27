package br.edu.cesar.filafood.apresentacao.entrega.entrega;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.aplicacao.entrega.entregador.EntregadorResumo;
import br.edu.cesar.filafood.aplicacao.entrega.entregador.EntregadorServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.EntregaResumo;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.EntregaServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.ItemDaTimeline;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.ResultadoDaFalha;
import br.edu.cesar.filafood.apresentacao.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.apresentacao.layout.Acao;
import br.edu.cesar.filafood.apresentacao.layout.Badge;
import br.edu.cesar.filafood.apresentacao.layout.Cabecalho;
import br.edu.cesar.filafood.apresentacao.layout.Campo;
import br.edu.cesar.filafood.apresentacao.layout.Entradas;
import br.edu.cesar.filafood.apresentacao.layout.Identificadores;
import br.edu.cesar.filafood.apresentacao.layout.MainLayout;
import br.edu.cesar.filafood.apresentacao.layout.Tabela;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.entrega.entrega.DesfechoDaFalha;
import br.edu.cesar.filafood.dominio.entrega.entrega.EstadoDaEntrega;
import br.edu.cesar.filafood.dominio.entrega.entrega.MotivoDaFalha;

/** Tela do entregador: entregas atribuídas, retirada, entrega e falha (F2 RN 5 e RN 6). */
@Route(value = "minhas-entregas", layout = MainLayout.class)
@PageTitle("FilaFood · Minhas entregas")
public class MinhasEntregasRota extends Div {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final EntregaServicoAplicacao entregas;
    private final EntregadorServicoAplicacao entregadores;
    private final Select<EntregadorResumo> seletor = new Select<>();
    private final Select<EntregaResumo> entregaSeletor = new Select<>();
    private boolean atualizandoSeletor;
    private final Div lista = new Div();
    private final Div painel = new Div();
    private final Paragraph aviso = new Paragraph();
    private List<EntregaResumo> minhas = List.of();
    private String selecionadaId;

    public MinhasEntregasRota(EntregaServicoAplicacao entregas, EntregadorServicoAplicacao entregadores) {
        this.entregas = entregas;
        this.entregadores = entregadores;

        List<EntregadorResumo> ativos = entregadores.pesquisarResumos(UnidadeDeDemonstracao.ID).stream()
                .filter(EntregadorResumo::ativo)
                .toList();
        seletor.setItems(ativos);
        seletor.setItemLabelGenerator(EntregadorResumo::nome);
        seletor.setWidth("240px");
        if (!ativos.isEmpty()) {
            seletor.setValue(ativos.get(0));
        }
        seletor.addValueChangeListener(evento -> {
            selecionadaId = null;
            recarregar();
        });

        aviso.setVisible(false);
        entregaSeletor.setWidth("360px");
        entregaSeletor.setItemLabelGenerator(entrega -> Identificadores.pedido(entrega.pedido())
                + " · " + entrega.estado().name().toLowerCase().replace('_', ' ') + " · " + entrega.endereco());
        entregaSeletor.addValueChangeListener(evento -> {
            if (!atualizandoSeletor && evento.getValue() != null) {
                selecionadaId = evento.getValue().id();
                render();
            }
        });


        Div barra = new Div();
        barra.addClassName("barra");
        barra.add(seletor, entregaSeletor);

        Paragraph rodape = new Paragraph("Primeiro registre a retirada; só depois a entrega ou a falha ficam "
                + "liberadas. A entrega exige o nome do recebedor e a falha exige motivo e observação.");
        rodape.addClassName("rodape");

        add(new Cabecalho("Minhas entregas",
                "Entregas atribuídas a mim, com o registro da retirada, da entrega ou da falha."), aviso, barra,
                lista, painel, rodape);
        recarregar();
    }

    private void recarregar() {
        if (seletor.getValue() == null) {
            minhas = List.of();
            selecionadaId = null;
            render();
            return;
        }
        minhas = entregas.listarDoEntregador(seletor.getValue().id().toString());
        boolean aindaExiste = selecionadaId != null
                && minhas.stream().anyMatch(entrega -> entrega.id().equals(selecionadaId));
        if (!aindaExiste) {
            selecionadaId = minhas.stream()
                    .filter(this::emAndamento)
                    .map(EntregaResumo::id)
                    .findFirst()
                    .orElse(minhas.isEmpty() ? null : minhas.get(0).id());
        }
        atualizandoSeletor = true;
        entregaSeletor.setItems(minhas);
        entregaSeletor.setValue(selecionada());
        atualizandoSeletor = false;
        render();
    }

    private void render() {
        lista.removeAll();
        painel.removeAll();

        Div badges = new Div();
        badges.addClassName("badges");
        badges.addClassName("pills-tabela");
        badges.add(Badge.neutro(minhas.size() + " em andamento"));
        lista.add(badges);

        Div card = new Div();
        card.addClassName("card");
        card.add(titulo("Entregas atribuídas a mim"));

        if (minhas.isEmpty()) {
            Paragraph vazio = new Paragraph("Nenhuma entrega atribuída a este entregador.");
            vazio.addClassName("vazio");
            card.add(vazio);
            lista.add(card);
            return;
        }

        Tabela tabela = new Tabela("Pedido", "Endereço", "Estado", "Ações");
        for (EntregaResumo entrega : minhas) {
            Div acoes = new Div();
            acoes.addClassName("acoes");
            acoes.addClassName("acoes-entregador");
            if (entrega.estado() == EstadoDaEntrega.DESPACHADA) {
                acoes.add(Acao.link("Registrar retirada", () -> registrarRetirada(entrega)));
            }
            if (entrega.id().equals(selecionadaId)) {
                acoes.add(new Span("selecionada"));
            } else if (emAndamento(entrega)) {
                acoes.add(Acao.link("Selecionar", () -> selecionar(entrega)));
            }
            tabela.linha(new Span(Identificadores.pedido(entrega.pedido())), new Span(entrega.endereco()),
                    badge(entrega.estado()), acoes);
        }
        card.add(tabela);
        lista.add(card);

        EntregaResumo selecionada = selecionada();
        if (selecionada == null) {
            return;
        }
        lista.add(timeline(selecionada));
        painel.add(acaoDaEntrega(selecionada));
    }

    private Div acaoDaEntrega(EntregaResumo entrega) {
        return switch (entrega.estado()) {
            case DESPACHADA -> cardRetirada(entrega);
            case EM_ROTA -> formulario(entrega);
            default -> cardSemAcao();
        };
    }

    private Div cardRetirada(EntregaResumo entrega) {
        Div card = new Div();
        card.addClassName("card");
        card.add(titulo("Retirada da entrega " + Identificadores.pedido(entrega.pedido())));
        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Paragraph("A entrega está despachada. Registre a retirada para poder registrar a entrega "
                + "ou uma falha."));
        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(Acao.primario("Registrar retirada", () -> registrarRetirada(entrega)));
        corpo.add(acoes);
        card.add(corpo);
        return card;
    }

    private Div cardSemAcao() {
        Div card = new Div();
        card.addClassName("card");
        card.add(titulo("Sem ação disponível"));
        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Paragraph("Esta entrega não está disponível para retirada, entrega ou falha."));
        card.add(corpo);
        return card;
    }

    private Div timeline(EntregaResumo entrega) {
        Div card = new Div();
        card.addClassName("card");
        card.add(titulo("Timeline da entrega " + Identificadores.pedido(entrega.pedido())));
        Element ul = new Element("ul");
        ul.getClassList().add("linha-tempo");
        for (int i = 0; i < entrega.timeline().size(); i++) {
            ItemDaTimeline transicao = entrega.timeline().get(i);
            Element li = new Element("li");
            li.getClassList().add(i == entrega.timeline().size() - 1 ? "atual" : "feito");
            Element span = new Element("span");
            span.getClassList().add("lt-titulo");
            span.setText(transicao.estado().name().toLowerCase().replace('_', ' '));
            li.appendChild(span);
            li.appendChild(new Element("span").setText(" · " + DATA.format(transicao.dataHora())));
            ul.appendChild(li);
        }
        card.getElement().appendChild(ul);
        return card;
    }

    private Div formulario(EntregaResumo entrega) {
        Div card = new Div();
        card.addClassName("card");
        card.add(titulo("Registrar na entrega " + Identificadores.pedido(entrega.pedido())));

        Input recebedor = Entradas.texto();
        Input observacao = Entradas.texto();
        Select<MotivoDaFalha> motivo = new Select<>();
        motivo.setItems(MotivoDaFalha.values());
        motivo.setItemLabelGenerator(MinhasEntregasRota::motivo);
        motivo.setValue(MotivoDaFalha.PROBLEMA_NO_TRANSPORTE);

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Campo("Recebedor", recebedor), new Campo("Motivo da falha", motivo),
                new Campo("Observação", observacao));

        Div botoes = new Div();
        botoes.addClassName("acoes-form");
        botoes.add(Acao.primario("Registrar entrega", () -> registrarEntrega(entrega, recebedor)),
                Acao.perigo("Registrar falha", () -> registrarFalha(entrega, motivo, observacao)));
        card.add(corpo, botoes);
        return card;
    }

    private void selecionar(EntregaResumo entrega) {
        selecionadaId = entrega.id();
        atualizandoSeletor = true;
        entregaSeletor.setValue(entrega);
        atualizandoSeletor = false;
        render();
    }

    private void registrarRetirada(EntregaResumo entrega) {
        executar(() -> entregas.registrarRetirada(entrega.id()), "Retirada registrada. A entrega está em rota.");
    }

    private void registrarEntrega(EntregaResumo entrega, Input recebedor) {
        executar(() -> entregas.registrarEntrega(entrega.id(), recebedor.getValue()), "Entrega registrada.");
    }

    private void registrarFalha(EntregaResumo entrega, Select<MotivoDaFalha> motivo, Input observacao) {
        try {
            ResultadoDaFalha resultado = entregas.registrarFalha(entrega.id(), motivo.getValue(),
                    observacao.getValue());
            recarregar();
            mostrar(resultado.desfecho() == DesfechoDaFalha.NOVA_TENTATIVA
                    ? "Falha registrada. Nova tentativa criada para o pedido "
                            + Identificadores.pedido(resultado.pedido()) + "."
                    : "Falha registrada. O pedido " + Identificadores.pedido(resultado.pedido())
                            + " foi cancelado.", true);
        } catch (RegraDeNegocioException erro) {
            mostrar(erro.getMessage(), false);
        }
    }

    private void executar(Runnable acao, String sucesso) {
        try {
            acao.run();
            recarregar();
            mostrar(sucesso, true);
        } catch (RegraDeNegocioException erro) {
            mostrar(erro.getMessage(), false);
        }
    }

    private void mostrar(String texto, boolean sucesso) {
        aviso.setText(texto);
        aviso.removeClassNames("aviso-ok", "aviso-erro");
        aviso.addClassName(sucesso ? "aviso-ok" : "aviso-erro");
        aviso.setVisible(true);
    }

    private boolean emAndamento(EntregaResumo entrega) {
        return entrega.estado() == EstadoDaEntrega.DESPACHADA || entrega.estado() == EstadoDaEntrega.EM_ROTA;
    }

    private EntregaResumo selecionada() {
        return minhas.stream().filter(entrega -> entrega.id().equals(selecionadaId)).findFirst().orElse(null);
    }

    private static Div titulo(String texto) {
        Div titulo = new Div();
        titulo.addClassName("card-titulo");
        titulo.add(new Span(texto));
        return titulo;
    }

    private static Span badge(EstadoDaEntrega estado) {
        String texto = estado.name().toLowerCase().replace('_', ' ');
        return switch (estado) {
            case DESPACHADA, EM_ROTA -> Badge.alerta(texto);
            case ENTREGUE -> Badge.ok(texto);
            case FALHA -> Badge.repor(texto);
            case AGUARDANDO -> Badge.neutro(texto);
        };
    }

    private static String motivo(MotivoDaFalha motivo) {
        return switch (motivo) {
            case AUSENCIA_DO_CLIENTE -> "ausência do cliente";
            case ENDERECO_NAO_ENCONTRADO -> "endereço não encontrado";
            case RECUSA_DO_CLIENTE -> "recusa do cliente";
            case PROBLEMA_NO_TRANSPORTE -> "problema no transporte";
        };
    }

    private static String dinheiro(BigDecimal valor) {
        return valor == null ? "—" : "R$ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
