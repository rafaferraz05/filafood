-- D-16: pedido de referência enquanto o contexto de atendimento (F4) não existe (ADR 0011).
-- A F1 só precisa do que a baixa (RN 4) e a devolução (RN 5) consultam: unidade, estado e itens.
create table pedido (
    id uuid primary key,
    unidade_id uuid not null,
    estado varchar(20) not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint ck_pedido_estado check (estado in ('RASCUNHO', 'ENVIADO', 'EM_PREPARO', 'CONCLUIDO',
                                                  'RECUSADO', 'CANCELADO'))
);

create table item_pedido (
    id uuid primary key,
    pedido_id uuid not null,
    produto_id uuid not null,
    quantidade integer not null,
    constraint fk_item_pedido foreign key (pedido_id) references pedido (id) on delete cascade,
    constraint fk_item_produto foreign key (produto_id) references produto (id),
    constraint ck_item_pedido_quantidade check (quantidade > 0)
);

create index ix_item_pedido_pedido on item_pedido (pedido_id);