-- D-14: ficha técnica da F1 (produto de referência, ficha por unidade e itens por insumo).
-- O produto é referência ao cardápio (F6), que ainda não tem tabela própria; aqui ele entra com o
-- nome que a tela mostra. As recusas da RN 2 são do domínio, não do banco: o schema garante apenas
-- o que é estrutural (quantidade positiva, insumo único por ficha, ficha única por produto/unidade).
create table produto (
    id uuid primary key,
    unidade_id uuid not null,
    nome varchar(120) not null,
    preco numeric(12,2) not null,
    estado varchar(20) not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint ck_produto_preco check (preco > 0),
    constraint ck_produto_estado check (estado in ('ATIVO', 'INATIVO')),
    constraint uk_produto_unidade_nome unique (unidade_id, nome)
);

create table ficha_tecnica (
    id uuid primary key,
    unidade_id uuid not null,
    produto_id uuid not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now(),
    constraint fk_ficha_produto foreign key (produto_id) references produto (id),
    constraint uk_ficha_unidade_produto unique (unidade_id, produto_id)
);

create table item_ficha_tecnica (
    id uuid primary key,
    ficha_id uuid not null,
    insumo_id uuid not null,
    quantidade numeric(12,3) not null,
    constraint fk_item_ficha foreign key (ficha_id) references ficha_tecnica (id) on delete cascade,
    constraint fk_item_insumo foreign key (insumo_id) references insumo (id),
    constraint ck_item_ficha_quantidade check (quantidade > 0),
    constraint uk_item_ficha_insumo unique (ficha_id, insumo_id)
);

create index ix_ficha_unidade on ficha_tecnica (unidade_id);
