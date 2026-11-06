package br.edu.cesar.filafood.app.adapter.in.web;

import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.app.adapter.in.web.layout.Acao;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Badge;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Busca;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Cabecalho;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Campo;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Entradas;
import br.edu.cesar.filafood.app.adapter.in.web.layout.MainLayout;
import br.edu.cesar.filafood.app.adapter.in.web.layout.Tabela;
import br.edu.cesar.filafood.app.application.estoque.AlterarEstadoDoInsumo;
import br.edu.cesar.filafood.app.application.estoque.CadastrarInsumo;
import br.edu.cesar.filafood.app.application.estoque.InsumoResumo;
import br.edu.cesar.filafood.app.application.estoque.ListarInsumos;
import br.edu.cesar.filafood.app.application.estoque.NovoInsumo;
import br.edu.cesar.filafood.app.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;

/** Tela de Insumos no layout e no CSS do protótipo (F1, D-12). */
@Route(value = "insumos", layout = MainLayout.class)
@PageTitle("FilaFood · Insumos")
public class InsumosView extends Div {

    private static final String RODAPE = "O insumo fica a repor quando o saldo é menor ou igual ao ponto de "
            + "pedido, que é o consumo médio diário vezes o prazo de entrega mais o estoque mínimo. "
            + "Insumo inativo não é monitorado: não há consumo a repor.";

    private final ListarInsumos listar;
    private final CadastrarInsumo cadastrar;
    private final AlterarEstadoDoInsumo alterarEstado;

    private final Cabecalho cabecalho = new Cabecalho("Insumos",
            "Estoque da unidade, com saldo, estoque mínimo e ponto de pedido.");
    private final Busca busca = new Busca("Buscar por nome");
    private final Select<String> filtro = new Select<>();
    private final Div lista = new Div();
    private final Paragraph aviso = new Paragraph();
    private List<InsumoResumo> insumos = List.of();

    public InsumosView(ListarInsumos listar, CadastrarInsumo cadastrar, AlterarEstadoDoInsumo alterarEstado) {
        this.listar = listar;
        this.cadastrar = cadastrar;
        this.alterarEstado = alterarEstado;

        busca.campo().addValueChangeListener(evento -> render());
        filtro.setItems("Estado: todos", "Estado: normal", "Estado: a repor", "Estado: inativo");
        filtro.setValue("Estado: todos");
        filtro.addClassName("filtro");
        filtro.setWidth("210px");
        filtro.addValueChangeListener(evento -> render());

        aviso.setVisible(false);

        Div barra = new Div();
        barra.addClassName("barra");
        barra.add(busca, filtro, Acao.primario("Novo insumo", this::abrirCadastro));

        Paragraph rodape = new Paragraph(RODAPE);
        rodape.addClassName("rodape");

        add(cabecalho, aviso, barra, lista, rodape);
        recarregar();
    }

    private void recarregar() {
        insumos = listar.executar(UnidadeDeDemonstracao.ID);
        render();
    }

    private void render() {
        long ativos = insumos.stream().filter(InsumoResumo::ativo).count();
        long aRepor = insumos.stream().filter(insumo -> insumo.ativo() && insumo.aRepor()).count();
        cabecalho.badges(Badge.neutro(ativos + " insumos ativos"), Badge.repor(aRepor + " a repor"));

        List<InsumoResumo> visiveis = filtrar();
        lista.removeAll();
        if (visiveis.isEmpty()) {
            Paragraph vazio = new Paragraph("Nenhum insumo encontrado para este filtro.");
            vazio.addClassName("vazio");
            lista.add(vazio);
            return;
        }

        Tabela tabela = new Tabela("Insumo", "Saldo", "Estoque mínimo", "Ponto de pedido", "Estado", "Ações");
        visiveis.forEach(insumo -> tabela.linha(
                nome(insumo),
                new Span(texto(insumo.saldo())),
                new Span(texto(insumo.estoqueMinimo())),
                new Span(texto(insumo.pontoDePedido())),
                estado(insumo),
                acoes(insumo)));
        lista.add(tabela);
    }

    private List<InsumoResumo> filtrar() {
        String termo = busca.campo().getValue() == null ? ""
                : busca.campo().getValue().strip().toLowerCase(Locale.ROOT);
        String estado = filtro.getValue() == null ? "Estado: todos" : filtro.getValue();
        return insumos.stream()
                .filter(insumo -> insumo.nome().toLowerCase(Locale.ROOT).contains(termo))
                .filter(insumo -> switch (estado) {
                    case "Estado: normal" -> insumo.ativo() && !insumo.aRepor();
                    case "Estado: a repor" -> insumo.ativo() && insumo.aRepor();
                    case "Estado: inativo" -> !insumo.ativo();
                    default -> true;
                })
                .toList();
    }

    private Component nome(InsumoResumo insumo) {
        Span nome = new Span(insumo.nome());
        nome.addClassName("destaque");
        Span unidade = new Span(insumo.unidadeDeMedida());
        unidade.addClassName("unidade");
        nome.add(unidade);
        return nome;
    }

    private Component estado(InsumoResumo insumo) {
        if (!insumo.ativo()) {
            return Badge.neutro("inativo");
        }
        return insumo.aRepor() ? Badge.repor("a repor") : Badge.ok("normal");
    }

    private Component acoes(InsumoResumo insumo) {
        Div acoes = new Div();
        acoes.addClassName("acoes");
        acoes.add(Acao.link("Entrada",
                () -> getUI().ifPresent(ui -> ui.navigate("insumos/" + insumo.id()))));
        acoes.add(insumo.ativo()
                ? Acao.apagada("Desativar", () -> alterar(insumo, false))
                : Acao.apagada("Reativar", () -> alterar(insumo, true)));
        return acoes;
    }

    private void alterar(InsumoResumo insumo, boolean ativo) {
        try {
            if (ativo) {
                alterarEstado.reativar(insumo.id().toString());
            } else {
                alterarEstado.desativar(insumo.id().toString());
            }
            aviso(ativo ? "Insumo reativado." : "Insumo desativado.", true);
            recarregar();
        } catch (RegraDeNegocioException excecao) {
            aviso(excecao.getMessage(), false);
        }
    }

    private void abrirCadastro() {
        Input nome = Entradas.texto();
        Input unidade = Entradas.texto();
        Input minimo = Entradas.texto();
        Input prazo = Entradas.texto();

        Dialog dialogo = new Dialog();
        dialogo.setWidth("420px");
        dialogo.getElement().getStyle().set("padding", "0");

        Div caixa = new Div();
        caixa.addClassName("confirmacao");

        Div titulo = new Div();
        titulo.addClassName("confirmacao-titulo");
        titulo.setText("Novo insumo");

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Campo("Nome", nome), new Campo("Unidade de medida", unidade),
                new Campo("Estoque mínimo", minimo), new Campo("Prazo de entrega (dias)", prazo));

        Div acoesForm = new Div();
        acoesForm.addClassName("acoes-form");
        acoesForm.add(
                Acao.primario("Salvar", () -> salvar(dialogo, nome, unidade, minimo, prazo)),
                Acao.claro("Cancelar", dialogo::close));

        caixa.add(titulo, corpo, acoesForm);
        dialogo.add(caixa);
        dialogo.open();
    }

    private void salvar(Dialog dialogo, Input nome, Input unidade, Input minimo, Input prazo) {
        try {
            cadastrar.executar(new NovoInsumo(UnidadeDeDemonstracao.ID, nome.getValue(), unidade.getValue(),
                    numero(minimo.getValue()), inteiro(prazo.getValue())));
            dialogo.close();
            aviso("Insumo cadastrado.", true);
            recarregar();
        } catch (RegraDeNegocioException | NumberFormatException excecao) {
            aviso(excecao.getMessage(), false);
        }
    }

    private void aviso(String texto, boolean sucesso) {
        aviso.setText(texto);
        aviso.removeClassNames("aviso-ok", "aviso-erro");
        aviso.addClassName(sucesso ? "aviso-ok" : "aviso-erro");
        aviso.setVisible(true);
    }

    private static String texto(java.math.BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    private static double numero(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new NumberFormatException("informe um número");
        }
        return Double.parseDouble(valor.replace(',', '.').strip());
    }

    private static int inteiro(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new NumberFormatException("informe um número inteiro");
        }
        return Integer.parseInt(valor.strip());
    }
}
