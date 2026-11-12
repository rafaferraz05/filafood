package br.edu.cesar.filafood.app.adapter.in.web;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.app.adapter.in.web.layout.Acao;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Badge;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Cabecalho;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Campo;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Entradas;
import br.edu.cesar.filafood.app.adapter.in.web.layout.MainLayout;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Tabela;
import br.edu.cesar.filafood.app.application.estoque.DetalharInsumo;
import br.edu.cesar.filafood.app.application.estoque.EntradaDeInsumo;
import br.edu.cesar.filafood.app.application.estoque.InsumoDetalhe;
import br.edu.cesar.filafood.app.application.estoque.MovimentacaoResumo;
import br.edu.cesar.filafood.app.application.estoque.RegistrarEntradaDeInsumo;
import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/** Detalhe do insumo no layout e no CSS do protótipo (F1 RN 3, D-12). */
@Route(value = "insumos/:id", layout = MainLayout.class)
@PageTitle("FilaFood · Detalhe do insumo")
public class DetalheInsumoView extends Div implements BeforeEnterObserver {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final DetalharInsumo detalhar;
    private final RegistrarEntradaDeInsumo registrarEntrada;
    private final Paragraph aviso = new Paragraph();
    private String insumoId;

    public DetalheInsumoView(DetalharInsumo detalhar, RegistrarEntradaDeInsumo registrarEntrada) {
        this.detalhar = detalhar;
        this.registrarEntrada = registrarEntrada;
        aviso.setVisible(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        insumoId = evento.getRouteParameters().get("id").orElseThrow();
        montar();
    }

    private void montar() {
        InsumoDetalhe detalhe = detalhar.executar(insumoId);
        removeAll();
        aviso.setVisible(false);

        Div esquerda = new Div();
        esquerda.add(cardEntrada(detalhe));

        Div grid = new Div();
        grid.addClassName("grid-2");
        grid.add(esquerda, cardMovimentacoes(detalhe));

        Paragraph rodape = new Paragraph("A entrada soma a quantidade ao saldo e não pode ser alterada nem "
                + "excluída depois de gravada. Insumo inativo não é monitorado.");
        rodape.addClassName("rodape");

        add(cabecalho(detalhe), aviso, cardKpis(detalhe), grid, rodape);
    }

    private Cabecalho cabecalho(InsumoDetalhe detalhe) {
        Cabecalho cabecalho = new Cabecalho(detalhe.nome(),
                "Insumo da unidade " + UnidadeDeDemonstracao.NOME + " · unidade de medida "
                        + detalhe.unidadeDeMedida() + " · prazo de entrega de "
                        + detalhe.prazoDeEntregaEmDias() + " dias");
        cabecalho.badges(
                detalhe.ativo() ? Badge.ok("ativo") : Badge.neutro("inativo"),
                detalhe.aRepor() ? Badge.repor("a repor") : Badge.neutro("normal"));
        return cabecalho;
    }

    private Div cardKpis(InsumoDetalhe detalhe) {
        Div kpis = new Div();
        kpis.addClassName("kpis");
        kpis.add(
                kpi("Saldo", texto(detalhe.saldo()), detalhe.unidadeDeMedida()),
                kpi("Consumo médio diário", decimal(detalhe.consumoMedioDiario()), "média dos últimos 30 dias"),
                kpi("Estoque mínimo", texto(detalhe.estoqueMinimo()), detalhe.unidadeDeMedida()),
                kpi("Ponto de pedido", texto(detalhe.pontoDePedido()), "consumo × prazo + mínimo"),
                kpi("Custo da última entrada", moeda(detalhe.custoDaUltimaEntrada()),
                        "por " + detalhe.unidadeDeMedida()));

        Div card = new Div();
        card.addClassName("card");
        card.add(kpis);
        return card;
    }

    private Div kpi(String rotulo, String valor, String nota) {
        Div kpi = new Div();
        kpi.addClassName("kpi");

        Div rotuloEl = new Div();
        rotuloEl.addClassName("kpi-rotulo");
        rotuloEl.setText(rotulo);

        Div valorEl = new Div();
        valorEl.addClassName("kpi-valor");
        valorEl.setText(valor);

        Div notaEl = new Div();
        notaEl.addClassName("kpi-nota");
        notaEl.setText(nota);

        kpi.add(rotuloEl, valorEl, notaEl);
        return kpi;
    }

    private Div cardEntrada(InsumoDetalhe detalhe) {
        Input quantidade = Entradas.texto();
        Input custo = Entradas.texto();
        Input total = Entradas.texto();
        total.getElement().setAttribute("readonly", true);

        Runnable calcular = () -> {
            try {
                BigDecimal valor = numero(quantidade.getValue()).multiply(numero(custo.getValue()));
                total.setValue(moeda(valor));
            } catch (NumberFormatException excecao) {
                total.setValue("");
            }
        };
        quantidade.addValueChangeListener(evento -> calcular.run());
        custo.addValueChangeListener(evento -> calcular.run());

        Div campos = new Div();
        campos.addClassName("linha-campos");
        campos.add(new Campo("Quantidade", quantidade), new Campo("Custo unitário (R$)", custo),
                new Campo("Custo total", total));

        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(
                Acao.primario("Registrar entrada", () -> registrar(detalhe, quantidade, custo)),
                Acao.claro("Limpar", () -> {
                    quantidade.setValue("");
                    custo.setValue("");
                    total.setValue("");
                }));

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(campos, acoes);

        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Nova entrada por compra", "F1 RN 3"), corpo);
        return card;
    }

    private void registrar(InsumoDetalhe detalhe, Input quantidade, Input custo) {
        try {
            registrarEntrada.executar(new EntradaDeInsumo(detalhe.id().toString(),
                    numero(quantidade.getValue()), numero(custo.getValue())));
            montar();
            aviso("Entrada registrada.", true);
        } catch (RegraDeNegocioException | NumberFormatException excecao) {
            aviso(excecao.getMessage(), false);
        }
    }

    private Div cardMovimentacoes(InsumoDetalhe detalhe) {
        Tabela tabela = new Tabela("Data e hora", "Tipo", "Quantidade", "Origem");
        detalhe.movimentacoes().forEach(movimentacao -> tabela.linha(
                new Span(DATA_HORA.format(movimentacao.dataHora())),
                new Span(tipo(movimentacao)),
                new Span(texto(movimentacao.quantidade()) + " " + detalhe.unidadeDeMedida()),
                new Span(movimentacao.origem() == null || movimentacao.origem().isBlank()
                        ? "—"
                        : movimentacao.origem())));

        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Movimentações", detalhe.movimentacoes().size() + " recentes"), tabela);
        return card;
    }

    private Div tituloDoCard(String texto, String direita) {
        Div titulo = new Div();
        titulo.addClassName("card-titulo");
        titulo.add(new Span(texto));
        Span dir = new Span(direita);
        dir.addClassName("dir");
        titulo.add(dir);
        return titulo;
    }

    private String tipo(MovimentacaoResumo movimentacao) {
        return switch (movimentacao.tipo()) {
            case "ENTRADA" -> "Entrada";
            case "SAIDA" -> "Saída";
            default -> movimentacao.tipo();
        };
    }

    private void aviso(String texto, boolean sucesso) {
        aviso.setText(texto);
        aviso.removeClassNames("aviso-ok", "aviso-erro");
        aviso.addClassName(sucesso ? "aviso-ok" : "aviso-erro");
        aviso.setVisible(true);
    }

    private static String texto(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    private static String decimal(BigDecimal valor) {
        return String.format(PT_BR, "%.1f", valor.doubleValue());
    }

    private static String moeda(BigDecimal valor) {
        return "R$ " + String.format(PT_BR, "%.2f", valor);
    }

    private static BigDecimal numero(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new NumberFormatException("informe um número");
        }
        return new BigDecimal(valor.replace(',', '.').strip());
    }
}
