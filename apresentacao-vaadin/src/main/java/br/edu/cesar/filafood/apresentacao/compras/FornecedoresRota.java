package br.edu.cesar.filafood.apresentacao.compras;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import br.edu.cesar.filafood.apresentacao.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.apresentacao.layout.Acao;
import br.edu.cesar.filafood.apresentacao.layout.Badge;
import br.edu.cesar.filafood.apresentacao.layout.Busca;
import br.edu.cesar.filafood.apresentacao.layout.Cabecalho;
import br.edu.cesar.filafood.apresentacao.layout.Campo;
import br.edu.cesar.filafood.apresentacao.layout.Entradas;
import br.edu.cesar.filafood.apresentacao.layout.MainLayout;
import br.edu.cesar.filafood.apresentacao.layout.Tabela;
import br.edu.cesar.filafood.aplicacao.compras.AlteracaoDeFornecedor;
import br.edu.cesar.filafood.aplicacao.compras.AlteracaoDeItemDoCatalogo;
import br.edu.cesar.filafood.aplicacao.compras.AlterarEstadoDoFornecedor;
import br.edu.cesar.filafood.aplicacao.compras.AlterarItemDoCatalogo;
import br.edu.cesar.filafood.aplicacao.compras.CadastrarFornecedor;
import br.edu.cesar.filafood.aplicacao.compras.EditarFornecedor;
import br.edu.cesar.filafood.aplicacao.compras.FornecedorDetalhe;
import br.edu.cesar.filafood.aplicacao.compras.IncluirItemNoCatalogo;
import br.edu.cesar.filafood.aplicacao.compras.InsumoParaCatalogo;
import br.edu.cesar.filafood.aplicacao.compras.ItemDoCatalogoResumo;
import br.edu.cesar.filafood.aplicacao.compras.ListarFornecedores;
import br.edu.cesar.filafood.aplicacao.compras.NovoFornecedor;
import br.edu.cesar.filafood.aplicacao.compras.NovoItemDoCatalogo;

/**
 * Tela de Fornecedores (F7 RN 1 e RN 2): cadastro do fornecedor e o catálogo de insumos que ele vende.
 *
 * <p>O catálogo é montado com os insumos reais do estoque da unidade (F1): é daqui que saem as ofertas
 * que a tela de Compras compara na escolha do fornecedor (RN 4).
 */
@Route(value = "fornecedores", layout = MainLayout.class)
@PageTitle("FilaFood · Fornecedores")
public class FornecedoresRota extends Div {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final String RODAPE = "Cada fornecedor tem no máximo um item por insumo. Só insumos ativos do "
            + "estoque da unidade entram no catálogo. A alteração de preço vale para as ordens criadas depois dela; "
            + "fornecedor inativo não concorre na escolha das sugestões de compra.";

    private final ListarFornecedores listar;
    private final CadastrarFornecedor cadastrar;
    private final EditarFornecedor editar;
    private final AlterarEstadoDoFornecedor alterarEstado;
    private final IncluirItemNoCatalogo incluirItem;
    private final AlterarItemDoCatalogo alterarItem;

    private final Cabecalho cabecalho = new Cabecalho("Fornecedores e catálogo",
            "Fornecedores da rede e os insumos que cada um vende para a unidade " + UnidadeDeDemonstracao.NOME
                    + ", com preço unitário, quantidade mínima e prazo.");
    private final Busca busca = new Busca("Buscar por nome ou CNPJ");
    private final Select<String> filtro = new Select<>();
    private final Paragraph aviso = new Paragraph();
    private final Div conteudoDinamico = new Div();

    private List<FornecedorDetalhe> fornecedores = List.of();
    private List<InsumoParaCatalogo> insumosDisponiveis = List.of();
    private String fornecedorSelecionadoId;

    public FornecedoresRota(ListarFornecedores listar, CadastrarFornecedor cadastrar, EditarFornecedor editar,
            AlterarEstadoDoFornecedor alterarEstado, IncluirItemNoCatalogo incluirItem,
            AlterarItemDoCatalogo alterarItem) {
        this.listar = listar;
        this.cadastrar = cadastrar;
        this.editar = editar;
        this.alterarEstado = alterarEstado;
        this.incluirItem = incluirItem;
        this.alterarItem = alterarItem;

        busca.campo().addValueChangeListener(e -> render());
        filtro.setItems("Estado: todos", "Estado: ativos", "Estado: inativos");
        filtro.setValue("Estado: todos");
        filtro.addClassName("filtro");
        filtro.setWidth("190px");
        filtro.addValueChangeListener(e -> render());

        aviso.setVisible(false);

        RouterLink sugestoes = new RouterLink("Ver sugestões de compra", SugestoesDeCompraRota.class);
        sugestoes.addClassNames("btn", "btn-claro");

        Div barra = new Div();
        barra.addClassName("barra");
        barra.add(busca, filtro, Acao.primario("Novo fornecedor", this::abrirModalCadastro), sugestoes);

        Paragraph rodape = new Paragraph(RODAPE);
        rodape.addClassName("rodape");

        add(cabecalho, aviso, barra, conteudoDinamico, rodape);
        recarregar();
    }

    private void recarregar() {
        fornecedores = listar.executar(UnidadeDeDemonstracao.ID);
        insumosDisponiveis = listar.insumosParaCatalogo(UnidadeDeDemonstracao.ID);
        if (fornecedorSelecionadoId == null
                || fornecedores.stream().noneMatch(f -> f.id().equals(fornecedorSelecionadoId))) {
            fornecedorSelecionadoId = fornecedores.isEmpty() ? null : fornecedores.get(0).id();
        }
        render();
    }

    private void render() {
        long ativos = fornecedores.stream().filter(FornecedorDetalhe::ativo).count();
        long itens = fornecedores.stream().mapToLong(f -> f.itens().size()).sum();
        cabecalho.badges(Badge.neutro(ativos + " fornecedores ativos"), Badge.ok(itens + " itens de catálogo"));

        conteudoDinamico.removeAll();
        conteudoDinamico.add(cardTabelaFornecedores());

        fornecedores.stream()
                .filter(f -> f.id().equals(fornecedorSelecionadoId))
                .findFirst()
                .ifPresent(selecionado -> {
                    Div grid = new Div();
                    grid.addClassName("grid-2");
                    grid.add(cardCatalogo(selecionado), cardNovoItem(selecionado));
                    conteudoDinamico.add(grid);
                });
    }

    private Div cardTabelaFornecedores() {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Fornecedores (F7 RN 1)", "clique no nome para manter o catálogo"));

        List<FornecedorDetalhe> visiveis = filtrar();
        if (visiveis.isEmpty()) {
            Paragraph vazio = new Paragraph("Nenhum fornecedor encontrado para este filtro.");
            vazio.addClassName("vazio");
            card.add(vazio);
            return card;
        }

        Tabela tabela = new Tabela("Fornecedor", "CNPJ", "Contato", "Catálogo", "Estado", "Ações");
        for (FornecedorDetalhe fornecedor : visiveis) {
            tabela.linha(
                    celulaNome(fornecedor),
                    new Span(fornecedor.cnpj()),
                    celulaContato(fornecedor),
                    new Span(fornecedor.itens().isEmpty() ? "sem itens"
                            : fornecedor.itens().size() + (fornecedor.itens().size() == 1 ? " insumo" : " insumos")),
                    fornecedor.ativo() ? Badge.ok("ativo") : Badge.neutro("inativo"),
                    celulaAcoes(fornecedor));
        }
        card.add(tabela);
        return card;
    }

    private Component celulaNome(FornecedorDetalhe fornecedor) {
        if (fornecedor.id().equals(fornecedorSelecionadoId)) {
            Span destaque = new Span(fornecedor.nome());
            destaque.addClassName("destaque");
            return destaque;
        }
        return Acao.link(fornecedor.nome(), () -> selecionar(fornecedor));
    }

    private Component celulaContato(FornecedorDetalhe fornecedor) {
        Div contato = new Div();
        if (!fornecedor.telefone().isBlank()) {
            contato.add(new Span(telefone(fornecedor.telefone())));
        }
        if (!fornecedor.email().isBlank()) {
            Span email = new Span(fornecedor.email());
            email.addClassName("unidade");
            contato.add(email);
        }
        return contato;
    }

    private Component celulaAcoes(FornecedorDetalhe fornecedor) {
        Div acoes = new Div();
        acoes.addClassName("acoes");
        acoes.add(Acao.link("Catálogo", () -> selecionar(fornecedor)));
        acoes.add(Acao.link("Editar", () -> abrirModalEdicao(fornecedor)));
        acoes.add(fornecedor.ativo()
                ? Acao.apagada("Desativar", () -> alterarEstado(fornecedor, false))
                : Acao.apagada("Reativar", () -> alterarEstado(fornecedor, true)));
        return acoes;
    }

    private void selecionar(FornecedorDetalhe fornecedor) {
        fornecedorSelecionadoId = fornecedor.id();
        render();
    }

    private Div cardCatalogo(FornecedorDetalhe fornecedor) {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Catálogo · " + fornecedor.nome(), "F7 RN 2"));

        if (fornecedor.itens().isEmpty()) {
            Div corpo = new Div();
            corpo.addClassName("card-corpo");
            corpo.add(new Paragraph("Este fornecedor ainda não tem insumos no catálogo. Inclua ao lado os insumos "
                    + "que ele vende para que entrem na comparação das sugestões de compra."));
            card.add(corpo);
            return card;
        }

        Tabela tabela = new Tabela("Insumo", "Preço unitário", "Mínimo", "Prazo", "Ações");
        for (ItemDoCatalogoResumo item : fornecedor.itens()) {
            Div insumo = new Div();
            Span nome = new Span(item.insumoNome());
            nome.addClassName("destaque");
            Span medida = new Span(item.insumoAtivo() ? item.unidadeDeMedida() : item.unidadeDeMedida() + " · insumo inativo");
            medida.addClassName("unidade");
            insumo.add(nome, medida);

            Div acoes = new Div();
            acoes.addClassName("acoes");
            acoes.add(Acao.link("Editar", () -> abrirModalItem(fornecedor, item)),
                    Acao.apagada("Remover", () -> confirmarRemocao(fornecedor, item)));

            tabela.linha(
                    insumo,
                    new Span(moeda(item.precoUnitario())),
                    new Span(quantidade(item.quantidadeMinima()) + " " + item.unidadeDeMedida()),
                    new Span(dias(item.prazoDeEntregaEmDias())),
                    acoes);
        }
        card.add(tabela);
        return card;
    }

    private Div cardNovoItem(FornecedorDetalhe fornecedor) {
        Div card = new Div();
        card.addClassName("card");
        card.add(tituloDoCard("Incluir insumo no catálogo · " + fornecedor.nome(), "insumos do estoque"));

        Set<String> jaNoCatalogo = fornecedor.itens().stream()
                .map(ItemDoCatalogoResumo::insumoId)
                .collect(Collectors.toSet());
        List<InsumoParaCatalogo> opcoes = insumosDisponiveis.stream()
                .filter(insumo -> !jaNoCatalogo.contains(insumo.insumoId()))
                .toList();

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        if (opcoes.isEmpty()) {
            corpo.add(new Paragraph(insumosDisponiveis.isEmpty()
                    ? "Não há insumo ativo no estoque da unidade. Cadastre o insumo na tela de Insumos."
                    : "Todos os insumos ativos da unidade já estão no catálogo deste fornecedor."));
            card.add(corpo);
            return card;
        }

        Select<InsumoParaCatalogo> insumo = new Select<>();
        insumo.setItems(opcoes);
        insumo.setItemLabelGenerator(opcao -> opcao.nome() + " (" + opcao.unidadeDeMedida() + ")");
        insumo.setValue(opcoes.get(0));
        insumo.setWidthFull();
        Input preco = Entradas.texto();
        preco.setPlaceholder("Ex.: 0,45");
        Input minimo = Entradas.texto();
        minimo.setPlaceholder("Ex.: 20");
        Input prazo = Entradas.texto();
        prazo.setPlaceholder("Ex.: 4");

        Div campos = new Div();
        campos.addClassName("linha-campos");
        campos.add(
                new Campo("Preço unitário (R$)", preco),
                new Campo("Quantidade mínima", minimo),
                new Campo("Prazo (dias)", prazo));

        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(Acao.primario("Incluir no catálogo", () -> {
            try {
                InsumoParaCatalogo escolhido = insumo.getValue();
                incluirItem.executar(new NovoItemDoCatalogo(
                        fornecedor.id(),
                        escolhido == null ? null : escolhido.insumoId(),
                        numero(preco.getValue(), "o preço unitário"),
                        numero(minimo.getValue(), "a quantidade mínima"),
                        inteiro(prazo.getValue(), "o prazo em dias")));
                recarregar();
                mostrarAviso(escolhido.nome() + " incluído no catálogo de " + fornecedor.nome() + ".", true);
            } catch (IllegalArgumentException excecao) {
                mostrarAviso(excecao.getMessage(), false);
            }
        }));

        corpo.add(new Campo("Insumo", insumo), campos, acoes);
        card.add(corpo);
        return card;
    }

    private void abrirModalItem(FornecedorDetalhe fornecedor, ItemDoCatalogoResumo item) {
        Input preco = Entradas.texto();
        preco.setValue(item.precoUnitario().toPlainString());
        Input minimo = Entradas.texto();
        minimo.setValue(quantidade(item.quantidadeMinima()));
        Input prazo = Entradas.texto();
        prazo.setValue(String.valueOf(item.prazoDeEntregaEmDias()));

        abrirDialogo("Editar item · " + item.insumoNome() + " · " + fornecedor.nome(),
                List.of(new Campo("Preço unitário (R$)", preco), new Campo("Quantidade mínima", minimo),
                        new Campo("Prazo (dias)", prazo)),
                dialogo -> {
                    try {
                        alterarItem.executar(new AlteracaoDeItemDoCatalogo(
                                fornecedor.id(),
                                item.itemId(),
                                numero(preco.getValue(), "o preço unitário"),
                                numero(minimo.getValue(), "a quantidade mínima"),
                                inteiro(prazo.getValue(), "o prazo em dias")));
                        dialogo.close();
                        recarregar();
                        mostrarAviso("Condições de " + item.insumoNome() + " atualizadas.", true);
                    } catch (IllegalArgumentException excecao) {
                        mostrarAviso(excecao.getMessage(), false);
                    }
                });
    }

    private void confirmarRemocao(FornecedorDetalhe fornecedor, ItemDoCatalogoResumo item) {
        Dialog dialogo = new Dialog();
        dialogo.setWidth("420px");

        Div caixa = new Div();
        caixa.addClassName("confirmacao");

        Div titulo = new Div();
        titulo.addClassName("confirmacao-titulo");
        titulo.setText("Remover " + item.insumoNome() + " do catálogo?");

        Paragraph texto = new Paragraph(fornecedor.nome() + " deixa de concorrer nas sugestões de compra de "
                + item.insumoNome() + ". O item pode ser incluído de novo depois.");
        texto.addClassName("confirmacao-texto");

        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(
                Acao.perigo("Confirmar remoção", () -> {
                    try {
                        alterarItem.remover(fornecedor.id(), item.itemId());
                        dialogo.close();
                        recarregar();
                        mostrarAviso(item.insumoNome() + " removido do catálogo.", true);
                    } catch (IllegalArgumentException excecao) {
                        dialogo.close();
                        mostrarAviso(excecao.getMessage(), false);
                    }
                }),
                Acao.claro("Cancelar", dialogo::close));

        caixa.add(titulo, texto, acoes);
        dialogo.add(caixa);
        dialogo.open();
    }

    private void abrirModalCadastro() {
        Input nome = Entradas.texto();
        Input cnpj = Entradas.texto();
        cnpj.setPlaceholder("00.000.000/0000-00");
        Input telefone = Entradas.texto();
        telefone.setPlaceholder("(81) 3333-0000");
        Input email = Entradas.texto();

        abrirFormularioFornecedor("Novo fornecedor", nome, cnpj, telefone, email, dialogo -> {
            try {
                var id = cadastrar.executar(new NovoFornecedor(nome.getValue(), cnpj.getValue(),
                        telefone.getValue(), email.getValue()));
                fornecedorSelecionadoId = id.toString();
                dialogo.close();
                recarregar();
                mostrarAviso("Fornecedor cadastrado. Inclua os insumos que ele vende no catálogo.", true);
            } catch (IllegalArgumentException excecao) {
                mostrarAviso(excecao.getMessage(), false);
            }
        });
    }

    private void abrirModalEdicao(FornecedorDetalhe fornecedor) {
        Input nome = Entradas.texto();
        nome.setValue(fornecedor.nome());
        Input cnpj = Entradas.texto();
        cnpj.setValue(fornecedor.cnpj());
        Input telefone = Entradas.texto();
        telefone.setValue(fornecedor.telefone());
        Input email = Entradas.texto();
        email.setValue(fornecedor.email());

        abrirFormularioFornecedor("Editar fornecedor · " + fornecedor.nome(), nome, cnpj, telefone, email, dialogo -> {
            try {
                editar.executar(new AlteracaoDeFornecedor(fornecedor.id(), nome.getValue(), cnpj.getValue(),
                        telefone.getValue(), email.getValue()));
                dialogo.close();
                recarregar();
                mostrarAviso("Dados do fornecedor atualizados.", true);
            } catch (IllegalArgumentException excecao) {
                mostrarAviso(excecao.getMessage(), false);
            }
        });
    }

    private void abrirFormularioFornecedor(String tituloModal, Input nome, Input cnpj, Input telefone, Input email,
            Consumer<Dialog> aoSalvar) {
        abrirDialogo(tituloModal,
                List.of(new Campo("Nome do fornecedor", nome), new Campo("CNPJ", cnpj),
                        new Campo("Telefone (opcional se houver e-mail)", telefone),
                        new Campo("E-mail (opcional se houver telefone)", email)),
                aoSalvar);
    }

    private void abrirDialogo(String tituloModal, List<Component> campos, Consumer<Dialog> aoSalvar) {
        Dialog dialogo = new Dialog();
        dialogo.setWidth("440px");

        Div caixa = new Div();
        caixa.addClassName("confirmacao");

        Div titulo = new Div();
        titulo.addClassName("confirmacao-titulo");
        titulo.setText(tituloModal);

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        campos.forEach(corpo::add);

        Div acoes = new Div();
        acoes.addClassName("acoes-form");
        acoes.add(
                Acao.primario("Salvar", () -> aoSalvar.accept(dialogo)),
                Acao.claro("Cancelar", dialogo::close));

        caixa.add(titulo, corpo, acoes);
        dialogo.add(caixa);
        dialogo.open();
    }

    private void alterarEstado(FornecedorDetalhe fornecedor, boolean ativar) {
        try {
            if (ativar) {
                alterarEstado.reativar(fornecedor.id());
            } else {
                alterarEstado.desativar(fornecedor.id());
            }
            recarregar();
            mostrarAviso(ativar ? fornecedor.nome() + " reativado."
                    : fornecedor.nome() + " desativado: deixa de concorrer nas sugestões de compra.", true);
        } catch (IllegalArgumentException excecao) {
            mostrarAviso(excecao.getMessage(), false);
        }
    }

    private List<FornecedorDetalhe> filtrar() {
        String termo = busca.campo().getValue() == null ? ""
                : busca.campo().getValue().strip().toLowerCase(Locale.ROOT);
        String termoCnpj = termo.replaceAll("[.\\-/\\s]", "");
        String estado = filtro.getValue() == null ? "Estado: todos" : filtro.getValue();
        return fornecedores.stream()
                .filter(f -> f.nome().toLowerCase(Locale.ROOT).contains(termo)
                        || (!termoCnpj.isEmpty() && f.cnpj().replaceAll("[.\\-/]", "").contains(termoCnpj)))
                .filter(f -> switch (estado) {
                    case "Estado: ativos" -> f.ativo();
                    case "Estado: inativos" -> !f.ativo();
                    default -> true;
                })
                .toList();
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

    private void mostrarAviso(String texto, boolean sucesso) {
        aviso.setText(texto);
        aviso.removeClassNames("aviso-ok", "aviso-erro");
        aviso.addClassName(sucesso ? "aviso-ok" : "aviso-erro");
        aviso.setVisible(true);
    }

    static String moeda(BigDecimal valor) {
        return "R$ " + String.format(PT_BR, "%.2f", valor);
    }

    static String quantidade(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    static String dias(int dias) {
        return dias == 1 ? "1 dia" : dias + " dias";
    }

    private static String telefone(String algarismos) {
        if (algarismos.length() == 11) {
            return "(" + algarismos.substring(0, 2) + ") " + algarismos.substring(2, 7) + "-" + algarismos.substring(7);
        }
        if (algarismos.length() == 10) {
            return "(" + algarismos.substring(0, 2) + ") " + algarismos.substring(2, 6) + "-" + algarismos.substring(6);
        }
        return algarismos;
    }

    private static BigDecimal numero(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("informe " + campo);
        }
        try {
            return new BigDecimal(valor.replace(',', '.').strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("valor inválido para " + campo);
        }
    }

    private static int inteiro(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("informe " + campo);
        }
        try {
            return Integer.parseInt(valor.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("valor inválido para " + campo);
        }
    }
}
