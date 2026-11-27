-- Mais dois pedidos do protótipo para a lista de preparo da F1 (o #1337 continua no V902).
-- Ids com sufixo 1336/1335 para a interface exibir #1336/#1335.
insert into pedido (id, unidade_id, estado)
values ('00000000-0000-0000-0000-000000001336', '00000000-0000-0000-0000-000000000001', 'ENVIADO'),
       ('00000000-0000-0000-0000-000000001335', '00000000-0000-0000-0000-000000000001', 'ENVIADO')
on conflict (id) do nothing;

insert into item_pedido (id, pedido_id, produto_id, quantidade)
select gen_random_uuid(), p.id, pr.id, v.quantidade
from (values
    ('00000000-0000-0000-0000-000000001336', 'Hambúrguer Clássico', 1),
    ('00000000-0000-0000-0000-000000001336', 'Hambúrguer Duplo', 2),
    ('00000000-0000-0000-0000-000000001335', 'Hambúrguer Clássico', 3)
) as v(pedido_id, produto, quantidade)
join pedido p on p.id = v.pedido_id::uuid
join produto pr on pr.unidade_id = p.unidade_id and pr.nome = v.produto
where not exists (select 1 from item_pedido i where i.pedido_id = p.id and i.produto_id = pr.id);
