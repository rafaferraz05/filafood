package br.edu.cesar.filafood.apresentacao.entrega.despacho;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.aplicacao.entrega.despacho.DespachoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.despacho.EntregaEmAndamentoResumo;
import br.edu.cesar.filafood.aplicacao.entrega.despacho.FilaDaUnidadeResumo;
import br.edu.cesar.filafood.aplicacao.entrega.despacho.ItemDaFilaResumo;
import br.edu.cesar.filafood.aplicacao.entrega.entrega.ItemDaTimeline;
import br.edu.cesar.filafood.apresentacao.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.apresentacao.layout.Acao;
import br.edu.cesar.filafood.apresentacao.layout.Badge;
import br.edu.cesar.filafood.apresentacao.layout.Cabecalho;
import br.edu.cesar.filafood.apresentacao.layout.Identificadores;
import br.edu.cesar.filafood.apresentacao.layout.MainLayout;
import br.edu.cesar.filafood.apresentacao.layout.Tabela;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;

/** Tela do gestor: fila de despacho, entregas em andamento e timeline (F2 RN 4, 5 e 6). */
@Route(value = "despacho", layout = MainLayout.class)
@PageTitle("FilaFood · Despacho e entregas")
public class DespachoRota extends Div {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final DespachoServicoAplicacao servico;
    private final Div lista = new Div();
    private final Paragraph aviso = new Paragraph();

    public DespachoRota(DespachoServicoAplicacao servico) {
        this.servico = servico;
        aviso.addClassName("aviso-erro");
        aviso.setVisible(false);

        Div barra = new Div();
        barra.addClassName("barra");
        barra.add(Acao.primario("Despachar agora", this::despachar),
                Acao.claro("Atualizar", this::render));

        Paragraph rodape = new Paragraph("O pedido vai para o entregador ativo que atende a zona e está abaixo da "
                + "carga máxima: o de menor carga e, no empate, o que está há mais tempo sem receber entrega.");
        rodape.addClassName("rodape");

        add(new Cabecalho("Despacho e entregas",
                "Fila de pedidos prontos e entregas em andamento. O despacho é automático."), aviso, barra, lista,
                rodape);
        despachar();
    }

    private void despachar() {
        try {
            servico.despacharPendentes(UnidadeDeDemonstracao.ID);
            render();
        } catch (RegraDeNegocioException erro) {
            aviso.setText(erro.getMessage());
            aviso.setVisible(true);
            render();
        }
    }

    private void render() {
        FilaDaUnidadeResumo painel = servico.painel(UnidadeDeDemonstracao.ID);
        lista.removeAll();

        Div badges = new Div();
        badges.addClassName("badges");
        badges.add(Badge.repor(painel.fila().size() + " na fila"),
                Badge.ok(painel.emAndamento().size() + " em andamento"));
        lista.add(badges);

        Div grid = new Div();
        grid.addClassName("grid-2");
        grid.add(fila(painel.fila()), andamento(painel.emAndamento()));
        lista.add(grid);

        if (!painel.emAndamento().isEmpty()) {
            lista.add(timeline(painel.emAndamento().get(0)));
        }
    }

    private Div fila(List<ItemDaFilaResumo> fila) {
        Div card = new Div();
        card.addClassName("card");
        card.add(titulo("Fila de despacho"));
        Tabela tabela = new Tabela("Pedido", "Zona", "Espera", "Por que não foi despachado");
        for (ItemDaFilaResumo item : fila) {
            tabela.linha(new Span(Identificadores.pedido(item.pedido())), new Span(item.zona()),
                    new Span(item.esperaMinutos() + " min"), new Span(item.motivo()));
        }
        card.add(tabela);
        return card;
    }

    private Div andamento(List<EntregaEmAndamentoResumo> emAndamento) {
        Div card = new Div();
        card.addClassName("card");
        card.add(titulo("Entregas em andamento"));
        Tabela tabela = new Tabela("Entrega", "Entregador", "Estado", "Taxa");
        for (EntregaEmAndamentoResumo entrega : emAndamento) {
            tabela.linha(new Span(Identificadores.pedido(entrega.pedido())), new Span(entrega.entregador()),
                    new Span(entrega.estado().name().toLowerCase().replace('_', ' ')), new Span(dinheiro(entrega.taxa())));
        }
        card.add(tabela);
        return card;
    }

    private Div timeline(EntregaEmAndamentoResumo entrega) {
        Div card = new Div();
        card.addClassName("card");
        card.add(titulo("Entrega " + Identificadores.pedido(entrega.pedido()) + " · " + entrega.endereco() + " · " + dinheiro(entrega.taxa())));
        Element ul = new Element("ul");
        ul.getClassList().add("linha-tempo");
        List<ItemDaTimeline> transicoes = entrega.timeline();
        for (int i = 0; i < transicoes.size(); i++) {
            ItemDaTimeline transicao = transicoes.get(i);
            Element li = new Element("li");
            li.getClassList().add(i == transicoes.size() - 1 ? "atual" : "feito");
            Element titulo = new Element("span");
            titulo.getClassList().add("lt-titulo");
            titulo.setText(transicao.estado().name().toLowerCase().replace('_', ' '));
            li.appendChild(titulo);
            li.appendChild(new Element("span").setText(" · " + DATA.format(transicao.dataHora())));
            ul.appendChild(li);
        }
        card.getElement().appendChild(ul);
        return card;
    }

    private static Div titulo(String texto) {
        Div titulo = new Div();
        titulo.addClassName("card-titulo");
        titulo.add(new Span(texto));
        return titulo;
    }

    private static String dinheiro(BigDecimal valor) {
        if (valor == null) {
            return "—";
        }
        return "R$ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
