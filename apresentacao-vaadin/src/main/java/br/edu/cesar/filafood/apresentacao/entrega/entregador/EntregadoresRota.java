package br.edu.cesar.filafood.apresentacao.entrega.entregador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import br.edu.cesar.filafood.aplicacao.entrega.entregador.AlteracaoDeEntregador;
import br.edu.cesar.filafood.aplicacao.entrega.entregador.EntregadorDetalhe;
import br.edu.cesar.filafood.aplicacao.entrega.entregador.EntregadorResumo;
import br.edu.cesar.filafood.aplicacao.entrega.entregador.EntregadorServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.entrega.entregador.NovoEntregador;
import br.edu.cesar.filafood.apresentacao.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.apresentacao.layout.Acao;
import br.edu.cesar.filafood.apresentacao.layout.Badge;
import br.edu.cesar.filafood.apresentacao.layout.Busca;
import br.edu.cesar.filafood.apresentacao.layout.Cabecalho;
import br.edu.cesar.filafood.apresentacao.layout.Campo;
import br.edu.cesar.filafood.apresentacao.layout.Entradas;
import br.edu.cesar.filafood.apresentacao.layout.MainLayout;
import br.edu.cesar.filafood.apresentacao.layout.Tabela;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;

/** Tela de Entregadores (F2 RN 1): cadastro, edição, inativação e reativação. */
@Route(value = "entregadores", layout = MainLayout.class)
@PageTitle("FilaFood · Entregadores")
public class EntregadoresRota extends Div {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final EntregadorServicoAplicacao servico;
    private final Busca busca = new Busca("Buscar por nome");
    private final Select<String> filtro = new Select<>();
    private final Div lista = new Div();
    private final Paragraph aviso = new Paragraph();
    private List<EntregadorResumo> entregadores = List.of();

    public EntregadoresRota(EntregadorServicoAplicacao servico) {
        this.servico = servico;

        busca.campo().addValueChangeListener(evento -> render());
        filtro.setItems("Estado: todos", "Estado: ativos", "Estado: inativos");
        filtro.setValue("Estado: todos");
        filtro.addClassName("filtro");
        filtro.setWidth("210px");
        filtro.addValueChangeListener(evento -> render());

        aviso.addClassName("aviso-erro");
        aviso.setVisible(false);

        Div barra = new Div();
        barra.addClassName("barra");
        barra.add(busca, filtro, Acao.primario("Novo entregador", this::abrirCadastro));

        Paragraph rodape = new Paragraph("A carga em andamento conta as entregas despachadas e em rota. "
                + "Entregador inativo para de receber novas entregas, mas as que já estão em rota continuam com ele.");
        rodape.addClassName("rodape");

        add(new Cabecalho("Entregadores",
                "Cadastro da unidade, com zonas atendidas, carga em andamento e estado."), aviso, barra, lista,
                rodape);
        recarregar();
    }

    private void recarregar() {
        entregadores = servico.pesquisarResumos(UnidadeDeDemonstracao.ID);
        render();
    }

    private void render() {
        long ativos = entregadores.stream().filter(EntregadorResumo::ativo).count();
        String termo = busca.campo().getValue() == null ? "" : busca.campo().getValue().trim().toLowerCase();
        List<EntregadorResumo> filtrados = entregadores.stream()
                .filter(entregador -> termo.isEmpty() || entregador.nome().toLowerCase().contains(termo))
                .filter(entregador -> switch (filtro.getValue()) {
                    case "Estado: ativos" -> entregador.ativo();
                    case "Estado: inativos" -> !entregador.ativo();
                    default -> true;
                })
                .toList();

        lista.removeAll();
        Div resumo = new Div();
        resumo.addClassName("badges");
        resumo.addClassName("pills-tabela");
        resumo.add(Badge.neutro(entregadores.size() + " entregadores"), Badge.ok(ativos + " ativos"));
        lista.add(resumo);

        Tabela tabela = new Tabela("Entregador", "Zonas atendidas", "Carga em andamento",
                "Última entrega concluída", "Estado", "Ações");
        for (EntregadorResumo entregador : filtrados) {
            Div acoes = new Div();
            acoes.addClassName("acoes");
            acoes.add(Acao.link("Editar", () -> abrirEdicao(entregador)));
            if (entregador.ativo()) {
                acoes.add(Acao.apagada("Inativar", () -> inativar(entregador)));
            } else {
                acoes.add(Acao.link("Reativar", () -> reativar(entregador)));
            }
            tabela.linha(new Span(entregador.nome()), new Span(String.join(", ", entregador.zonasAtendidas())),
                    new Span(entregador.cargaEmAndamento() + " de " + entregador.cargaMaxima()),
                    new Span(ultima(entregador.ultimaEntregaEm())),
                    entregador.ativo() ? Badge.ok("ativo") : Badge.neutro("inativo"), acoes);
        }
        lista.add(tabela);
    }

    private void abrirCadastro() {
        abrirFormulario(null);
    }

    private void abrirEdicao(EntregadorResumo resumo) {
        abrirFormulario(servico.obterDetalhe(resumo.id().toString()));
    }

    private void abrirFormulario(EntregadorDetalhe detalhe) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(detalhe == null ? "Novo entregador" : "Editar entregador");

        Input nome = Entradas.texto();
        Input carga = Entradas.texto();
        Input zonas = Entradas.texto();
        if (detalhe != null) {
            nome.setValue(detalhe.nome());
            carga.setValue(String.valueOf(detalhe.cargaMaxima()));
            zonas.setValue(String.join(", ", detalhe.zonasAtendidas()));
        }
        zonas.setPlaceholder("Ex.: Centro, Boa Viagem");

        Div corpo = new Div();
        corpo.addClassName("card-corpo");
        corpo.add(new Campo("Nome", nome), new Campo("Carga máxima", carga),
                new Campo("Zonas atendidas (separadas por vírgula)", zonas));

        Div botoes = new Div();
        botoes.add(Acao.primario("Salvar", () -> salvar(detalhe, nome, carga, zonas, dialog)),
                Acao.claro("Cancelar", dialog::close));
        dialog.add(corpo, botoes);
        dialog.open();
    }

    private void salvar(EntregadorDetalhe detalhe, Input nome, Input carga, Input zonas, Dialog dialog) {
        try {
            Set<String> zonasAtendidas = new LinkedHashSet<>();
            for (String zona : zonas.getValue().split(",")) {
                if (!zona.isBlank()) {
                    zonasAtendidas.add(zona.trim());
                }
            }
            int cargaMaxima = Integer.parseInt(carga.getValue().trim());
            if (detalhe == null) {
                servico.cadastrar(new NovoEntregador(UnidadeDeDemonstracao.ID, nome.getValue(), zonasAtendidas,
                        cargaMaxima));
            } else {
                servico.alterar(new AlteracaoDeEntregador(detalhe.id().toString(), nome.getValue(), zonasAtendidas,
                        cargaMaxima));
            }
            dialog.close();
            aviso.setVisible(false);
            recarregar();
        } catch (RegraDeNegocioException | NumberFormatException erro) {
            aviso.setText("Não foi possível salvar: " + erro.getMessage());
            aviso.setVisible(true);
        }
    }

    private void inativar(EntregadorResumo entregador) {
        try {
            servico.desativar(entregador.id().toString());
            recarregar();
        } catch (RegraDeNegocioException erro) {
            aviso.setText(erro.getMessage());
            aviso.setVisible(true);
        }
    }

    private void reativar(EntregadorResumo entregador) {
        servico.reativar(entregador.id().toString());
        recarregar();
    }

    private static String ultima(LocalDateTime dataHora) {
        return dataHora == null ? "—" : DATA.format(dataHora);
    }
}
