-- D-08: primeira fatia do schema de F1 (insumo e movimentação de estoque). As entidades JPA
-- correspondentes entram no D-09; aqui só o schema, com as restrições que as regras exigem.
create table insumo (
    id uuid primary key,
    unidade_id uuid not null,
    nome varchar(120) not null,
    unidade_de_medida varchar(20) not null,
    estoque_minimo numeric(12,3) not null,
    prazo_de_entrega_em_dias integer not null,
    estado varchar(20) not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint ck_insumo_estoque_minimo check (estoque_minimo > 0),
    constraint ck_insumo_prazo check (prazo_de_entrega_em_dias >= 0),
    constraint ck_insumo_estado check (estado in ('ATIVO', 'INATIVO')),
    constraint uk_insumo_unidade_nome unique (unidade_id, nome)
);

create table movimentacao_estoque (
    id uuid primary key,
    insumo_id uuid not null,
    tipo varchar(10) not null,
    motivo varchar(20) not null,
    quantidade numeric(12,3) not null,
    custo_unitario numeric(12,2) not null,
    origem varchar(80) not null default '',
    data_hora timestamptz not null,
    constraint fk_movimentacao_insumo foreign key (insumo_id) references insumo (id),
    constraint ck_movimentacao_tipo check (tipo in ('ENTRADA', 'SAIDA')),
    constraint ck_movimentacao_motivo check (motivo in ('COMPRA', 'CONSUMO', 'DEVOLUCAO')),
    constraint ck_movimentacao_quantidade check (quantidade > 0),
    constraint ck_movimentacao_custo check (custo_unitario >= 0)
);

create index ix_movimentacao_insumo_data on movimentacao_estoque (insumo_id, data_hora);
