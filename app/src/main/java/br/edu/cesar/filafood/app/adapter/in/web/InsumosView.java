package br.edu.cesar.filafood.app.adapter.in.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.app.application.estoque.AlterarEstadoDoInsumo;
import br.edu.cesar.filafood.app.application.estoque.CadastrarInsumo;
import br.edu.cesar.filafood.app.application.estoque.EntradaDeInsumo;
import br.edu.cesar.filafood.app.application.estoque.InsumoResumo;
import br.edu.cesar.filafood.app.application.estoque.ListarInsumos;
import br.edu.cesar.filafood.app.application.estoque.NovoInsumo;
import br.edu.cesar.filafood.app.application.estoque.RegistrarEntradaDeInsumo;
import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/** Tela de Insumos: lista, busca, filtro "a repor", cadastro, entrada e estado (F1, D-12). */
@Route("insumos")
@PageTitle("Insumos")
public class InsumosView extends VerticalLayout {

    private final ListarInsumos listar;
    private final CadastrarInsumo cadastrar;
    private final RegistrarEntradaDeInsumo registrarEntrada;
    private final AlterarEstadoDoInsumo alterarEstado;

    private final Grid<InsumoResumo> grid = new Grid<>(InsumoResumo.class, false);
    private final TextField busca = new TextField("Buscar por nome");
    private final Checkbox somenteARepor = new Checkbox("Somente a repor");
    private List<InsumoResumo> insumos = List.of();

    public InsumosView(ListarInsumos listar, CadastrarInsumo cadastrar,
            RegistrarEntradaDeInsumo registrarEntrada, AlterarEstadoDoInsumo alterarEstado) {
        this.listar = listar;
        this.cadastrar = cadastrar;
        this.registrarEntrada = registrarEntrada;
        this.alterarEstado = alterarEstado;

        setPadding(true);
        setSpacing(true);
        configurarGrid();

        Button novo = new Button("Novo insumo", evento -> abrirCadastro());
        add(new H1("Insumos"), new HorizontalLayout(busca, somenteARepor, novo), grid);

        busca.addValueChangeListener(evento -> aplicarFiltro());
        somenteARepor.addValueChangeListener(evento -> aplicarFiltro());
        recarregar();
    }

    private void configurarGrid() {
        grid.addColumn(InsumoResumo::nome).setHeader("Insumo");
        grid.addColumn(InsumoResumo::unidadeDeMedida).setHeader("Unidade");
        grid.addColumn(insumo -> texto(insumo.saldo())).setHeader("Saldo");
        grid.addColumn(insumo -> texto(insumo.estoqueMinimo())).setHeader("Mínimo");
        grid.addColumn(insumo -> insumo.aRepor() ? "a repor" : "normal").setHeader("Reposição");
        grid.addColumn(insumo -> insumo.ativo() ? "ativo" : "inativo").setHeader("Estado");
        grid.addComponentColumn(this::acoes).setHeader("Ações");
    }

    private HorizontalLayout acoes(InsumoResumo insumo) {
        Button entrada = new Button("Entrada", evento -> abrirEntrada(insumo));
        Button estado = new Button(insumo.ativo() ? "Desativar" : "Reativar", evento -> {
            try {
                if (insumo.ativo()) {
                    alterarEstado.desativar(insumo.id().toString());
                } else {
                    alterarEstado.reativar(insumo.id().toString());
                }
                recarregar();
            } catch (RegraDeNegocioException excecao) {
                avisar(excecao.getMessage());
            }
        });
        Button detalhe = new Button("Detalhe", evento -> getUI().ifPresent(ui -> ui.navigate("insumos/" + insumo.id())));
        return new HorizontalLayout(entrada, estado, detalhe);
    }

    private void abrirCadastro() {
        TextField nome = new TextField("Nome");
        TextField unidade = new TextField("Unidade de medida");
        TextField minimo = new TextField("Estoque mínimo");
        TextField prazo = new TextField("Prazo de entrega (dias)");

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Novo insumo");
        Button salvar = new Button("Salvar", evento -> {
            try {
                cadastrar.executar(new NovoInsumo(UnidadeDeDemonstracao.ID, nome.getValue(), unidade.getValue(),
                        numero(minimo.getValue()), inteiro(prazo.getValue())));
                dialogo.close();
                recarregar();
            } catch (RegraDeNegocioException | NumberFormatException excecao) {
                avisar(excecao.getMessage());
            }
        });
        dialogo.add(new VerticalLayout(nome, unidade, minimo, prazo, salvar));
        dialogo.open();
    }

    private void abrirEntrada(InsumoResumo insumo) {
        TextField quantidade = new TextField("Quantidade");
        TextField custo = new TextField("Custo unitário");

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Entrada de " + insumo.nome());
        Button salvar = new Button("Registrar", evento -> {
            try {
                registrarEntrada.executar(new EntradaDeInsumo(insumo.id().toString(), numero(quantidade.getValue()),
                        BigDecimal.valueOf(numero(custo.getValue()))));
                dialogo.close();
                recarregar();
            } catch (RegraDeNegocioException | NumberFormatException excecao) {
                avisar(excecao.getMessage());
            }
        });
        dialogo.add(new VerticalLayout(quantidade, custo, salvar));
        dialogo.open();
    }

    private void recarregar() {
        insumos = listar.executar(UnidadeDeDemonstracao.ID);
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        String termo = busca.getValue() == null ? "" : busca.getValue().strip().toLowerCase(Locale.ROOT);
        grid.setItems(insumos.stream()
                .filter(insumo -> insumo.nome().toLowerCase(Locale.ROOT).contains(termo))
                .filter(insumo -> !somenteARepor.getValue() || insumo.aRepor())
                .toList());
    }

    private void avisar(String mensagem) {
        Notification.show(mensagem, 4000, Notification.Position.MIDDLE);
    }

    private static String texto(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    private static double numero(String valor) {
        return Double.parseDouble(valor.replace(',', '.').strip());
    }

    private static int inteiro(String valor) {
        return Integer.parseInt(valor.strip());
    }
}
