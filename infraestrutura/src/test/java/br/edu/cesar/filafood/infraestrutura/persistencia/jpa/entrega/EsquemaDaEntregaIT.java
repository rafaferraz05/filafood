package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Migração da F2 em Postgres real: tabelas e índices da entrega. Roda com {@code -Pintegracao}. */
@Testcontainers
class EsquemaDaEntregaIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void migraESchemaDaEntregaComOsIndicesDeInvariante() throws Exception {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration", "classpath:db/seed")
                .load()
                .migrate();

        try (Connection conexao = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword()); Statement comando = conexao.createStatement()) {
            for (String tabela : new String[] { "entregador", "entregador_zona", "zona_entrega", "entrega",
                    "transicao_entrega" }) {
                try (ResultSet resultado = comando.executeQuery(
                        "select to_regclass('public." + tabela + "')")) {
                    resultado.next();
                    assertNotNull(resultado.getString(1), "tabela ausente: " + tabela);
                }
            }
            try (ResultSet resultado = comando.executeQuery(
                    "select indexname from pg_indexes where schemaname = 'public' and indexname = 'uk_entrega_ativa_pedido'")) {
                assertTrue(resultado.next(), "faltou o índice único de entrega ativa por pedido");
            }
        }
    }
}
