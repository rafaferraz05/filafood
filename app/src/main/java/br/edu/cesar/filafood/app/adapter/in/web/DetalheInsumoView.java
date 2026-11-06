package br.edu.cesar.filafood.app.adapter.in.web;

import java.math.BigDecimal;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.app.application.estoque.DetalharInsumo;
import br.edu.cesar.filafood.app.application.estoque.EntradaDeInsumo;
import br.edu.cesar.filafood.app.application.estoque.InsumoDetalhe;
import br.edu.cesar.filafood.app.application.estoque.MovimentacaoResumo;
import br.edu.cesar.filafood.app.application.estoque.RegistrarEntradaDeInsumo;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/** Detalhe do insumo com entrada e histórico (F1, D-12). */
@Route("insumos/:id")
@PageTitle("Detalhe do insumo")
public class DetalheInsumoView extends VerticalLayout implements BeforeEnterObserver {

    private final DetalharInsumo detalhar;
    private final RegistrarEntradaDeInsumo registrarEntrada;
    private String insumoId;

    public DetalheInsumoView(DetalharInsumo detalhar, RegistrarEntradaDeInsumo registrarEntrada) {
        this.detalhar = detalhar;
        this.registrarEntrada = registrarEntrada;
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        insumoId = evento.getRouteParameters().get("id").orElseThrow();
        montar();
    }

    private void montar() {
        removeAll();
        InsumoDetalhe detalhe = detalhar.executar(insumoId);

        Grid<MovimentacaoResumo> historico = new Grid<>(MovimentacaoResumo.class, false);
        historico.addColumn(MovimentacaoResumo::tipo).setHeader("Tipo");
        historico.addColumn(MovimentacaoResumo::motivo).setHeader("Motivo");
        historico.addColumn(movimentacao -> movimentacao.quantidade().toPlainString()).setHeader("Quantidade");
        historico.addColumn(movimentacao -> movimentacao.custoUnitario().toPlainString()).setHeader("Custo unitário");
        historico.addColumn(MovimentacaoResumo::origem).setHeader("Origem");
        historico.addColumn(movimentacao -> movimentacao.dataHora().toString()).setHeader("Data/hora");
        historico.setItems(detalhe.movimentacoes());

        Button entrada = new Button("Registrar entrada", evento -> abrirEntrada(detalhe));
        Button voltar = new Button("Voltar", evento -> getUI().ifPresent(ui -> ui.navigate("insumos")));

        add(new H1(detalhe.nome()),
                new Paragraph("Saldo: " + detalhe.saldo().toPlainString() + " " + detalhe.unidadeDeMedida()
                        + " | Mínimo: " + detalhe.estoqueMinimo().toPlainString()
                        + " | Ponto de pedido: " + detalhe.pontoDePedido().toPlainString()
                        + " | Prazo: " + detalhe.prazoDeEntregaEmDias() + " dias"
                        + " | Estado: " + (detalhe.ativo() ? "ativo" : "inativo")),
                new HorizontalLayout(entrada, voltar),
                new Paragraph("Movimentações"),
                historico);
    }

    private void abrirEntrada(InsumoDetalhe detalhe) {
        TextField quantidade = new TextField("Quantidade");
        TextField custo = new TextField("Custo unitário");

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Entrada de " + detalhe.nome());
        Button salvar = new Button("Registrar", evento -> {
            try {
                registrarEntrada.executar(new EntradaDeInsumo(detalhe.id().toString(),
                        Double.parseDouble(quantidade.getValue().replace(',', '.').strip()),
                        BigDecimal.valueOf(Double.parseDouble(custo.getValue().replace(',', '.').strip()))));
                dialogo.close();
                montar();
            } catch (RegraDeNegocioException | NumberFormatException excecao) {
                Notification.show(excecao.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });
        dialogo.add(new VerticalLayout(quantidade, custo, salvar));
        dialogo.open();
    }
}
