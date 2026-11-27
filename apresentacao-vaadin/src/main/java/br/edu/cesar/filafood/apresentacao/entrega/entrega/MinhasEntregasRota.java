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
import br.edu.cesar.filafood.aplicacao.entrega.entrega.ItemDaTimeline;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.EntregaServicoAplicacao;
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
    private final Div lista = new Div();
    private final Div painel = new Div();
    private final Paragraph aviso = new Paragraph();
    private List<EntregaResumo> minhas = List.of();
    private EntregaResumo selecionada;

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
        seletor.addValueChangeListener(evento -> recarregar());

        aviso.addClassName("aviso-erro");
        aviso.setVisible(false);

        Div barra = new Div();
        barra.addClassName("barra");
        barra.add(seletor);

        Paragraph rodape = new Paragraph("Não é possível pular etapa: a entrega precisa estar despachada para "
                + "registrar a retirada. Para registrar a entrega é obrigatório o nome do recebedor, e para "
                + "registrar a falha são obrigatórios o motivo e a observação.");
        rodape.addClassName("rodape");

        add(new Cabecalho("Minhas entregas",
                "Entregas atribuídas a mim, com o registro da retirada, da entrega ou da falha."), aviso, barra,
                lista, painel, rodape);
        recarregar();
    }

    private void recarregar() {
        if (seletor.getValue() == null) {
            return;
        }
        minhas = entregas.listarDoEntregador(seletor.getValue().id().toString());
        selecionada = minhas.stream().filter(entrega -> entrega.estado() != EstadoDaEntrega.ENTREGUE
                && entrega.estado() != EstadoDaEntrega.FALHA).findFirst().orElse(minhas.isEmpty() ? null
                        : minhas.get(0));
        render();
    }

    private void render() {
        lista.removeAll();
        painel.removeAll();

        Div badges = new Div();
        badges.addClassName("badges");
        badges.add(Badge.neutro(minhas.size() + " em andamento"));
        lista.add(badges);

        Div card = new Div();
        card.addClassName("card");
        Div titulo = new Div();
        titulo.addClassName("card-titulo");
        titulo.add(new Span("Entregas atribuídas a mim"));
        card.add(titulo);

        Tabela tabela = new Tabela("Entrega", "Endereço", "Estado", "Ações");
        for (EntregaResumo entrega : minhas) {
            Div acoes = new Div();
            acoes.addClassName("acoes");
            if (entrega.estado() == EstadoDaEntrega.DESPACHADA) {
                acoes.add(Acao.link("Registrar retirada", () -> registrarRetirada(entrega)));
            } else {
                acoes.add(new Span("—"));
            }
            tabela.linha(new Span(Identificadores.pedido(entrega.pedido())), new Span(entrega.endereco()),
                    badge(entrega.estado()), acoes);
        }
        card.add(tabela);
        lista.add(card);

        if (selecionada != null) {
            lista.add(timeline(selecionada));
            painel.add(formulario(selecionada));
        }
    }

    private Div timeline(EntregaResumo entrega) {
        Div card = new Div();
        card.addClassName("card");
        Div titulo = new Div();
        titulo.addClassName("card-titulo");
        titulo.add(new Span("Entrega " + Identificadores.pedido(entrega.pedido())));
        card.add(titulo);
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
        Div titulo = new Div();
        titulo.addClassName("card-titulo");
        titulo.add(new Span("Registrar na entrega " + Identificadores.pedido(entrega.pedido())));
        card.add(titulo);

        Input recebedor = Entradas.texto();
        Input observacao = Entradas.texto();
        Select<MotivoDaFalha> motivo = new Select<>();
        motivo.setItems(MotivoDaFalha.values());
        motivo.setItemLabelGenerator(MinhasEntregasRota::motivo);

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Campo("Recebedor", recebedor), new Campo("Motivo da falha", motivo),
                new Campo("Observação", observacao));

        Div botoes = new Div();
        botoes.add(Acao.primario("Registrar entrega", () -> registrarEntrega(entrega, recebedor)),
                Acao.perigo("Registrar falha", () -> registrarFalha(entrega, motivo, observacao)));
        card.add(corpo, botoes);
        return card;
    }

    private void registrarRetirada(EntregaResumo entrega) {
        executar(() -> entregas.registrarRetirada(entrega.id()));
    }

    private void registrarEntrega(EntregaResumo entrega, Input recebedor) {
        executar(() -> entregas.registrarEntrega(entrega.id(), recebedor.getValue()));
    }

    private void registrarFalha(EntregaResumo entrega, Select<MotivoDaFalha> motivo, Input observacao) {
        executar(() -> {
            ResultadoDaFalha resultado = entregas.registrarFalha(entrega.id(), motivo.getValue(),
                    observacao.getValue());
            aviso.setText(resultado.desfecho() == br.edu.cesar.filafood.dominio.entrega.entrega.DesfechoDaFalha.NOVA_TENTATIVA
                    ? "Falha registrada. Uma nova tentativa foi criada para o pedido " + Identificadores.pedido(resultado.pedido()) + "."
                    : "Falha registrada. O pedido " + Identificadores.pedido(resultado.pedido()) + " foi cancelado.");
            aviso.setVisible(true);
        });
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
            recarregar();
        } catch (RegraDeNegocioException erro) {
            aviso.setText(erro.getMessage());
            aviso.setVisible(true);
        }
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
