-- Seed de desenvolvimento: pedido #1337 do protótipo (2 Hambúrguer Clássico + 1 Duplo), no estado
-- ENVIADO, pronto para o preparo dar baixa. Só o perfil dev aponta o Flyway para classpath:db/seed.
-- Idempotente: pedido pelo id e item por pedido/produto.
insert into pedido (id, unidade_id, estado)
values ('00000000-0000-0000-0000-000000000501', '00000000-0000-0000-0000-000000000001', 'ENVIADO')
on conflict (id) do nothing;

insert into item_pedido (id, pedido_id, produto_id, quantidade)
select gen_random_uuid(), p.id, pr.id, v.quantidade
from (values ('Hambúrguer Clássico', 2), ('Hambúrguer Duplo', 1)) as v(produto, quantidade)
join pedido p on p.id = '00000000-0000-0000-0000-000000000501'
join produto pr on pr.unidade_id = p.unidade_id and pr.nome = v.produto
where not exists (select 1 from item_pedido i where i.pedido_id = p.id and i.produto_id = pr.id);