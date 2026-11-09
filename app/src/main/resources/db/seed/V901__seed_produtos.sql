-- Seed de desenvolvimento (D-19): produtos do cardápio e fichas técnicas dos protótipos. Só o perfil
-- dev aponta o Flyway para classpath:db/seed. Idempotente: produto pelo nome na unidade, ficha por
-- unidade/produto e item por ficha/insumo. A Batata Rústica fica sem ficha de propósito — é o estado
-- "sem ficha" do protótipo.
insert into produto (id, unidade_id, nome, preco, estado)
values
  ('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000001',
   'Hambúrguer Clássico', 29.90, 'ATIVO'),
  ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000001',
   'Hambúrguer Duplo', 39.90, 'ATIVO'),
  ('00000000-0000-0000-0000-000000000303', '00000000-0000-0000-0000-000000000001',
   'Batata Rústica', 18.90, 'ATIVO')
on conflict (unidade_id, nome) do nothing;

insert into ficha_tecnica (id, unidade_id, produto_id)
select v.id, p.unidade_id, p.id
from (values
        ('00000000-0000-0000-0000-000000000401'::uuid, 'Hambúrguer Clássico'),
        ('00000000-0000-0000-0000-000000000402'::uuid, 'Hambúrguer Duplo')
     ) as v(id, produto)
join produto p on p.unidade_id = '00000000-0000-0000-0000-000000000001' and p.nome = v.produto
on conflict (unidade_id, produto_id) do nothing;

-- As quantidades são as do protótipo (Hambúrguer Clássico: 1 un de pão, 0,15 kg de carne, 0,03 kg de
-- queijo e 0,02 kg de tomate; o Duplo dobra carne, queijo e tomate).
insert into item_ficha_tecnica (id, ficha_id, insumo_id, quantidade)
select gen_random_uuid(), f.id, i.id, v.quantidade
from (values
        ('Hambúrguer Clássico', 'Pão', 1.000),
        ('Hambúrguer Clássico', 'Carne', 0.150),
        ('Hambúrguer Clássico', 'Queijo', 0.030),
        ('Hambúrguer Clássico', 'Tomate', 0.020),
        ('Hambúrguer Duplo', 'Pão', 1.000),
        ('Hambúrguer Duplo', 'Carne', 0.300),
        ('Hambúrguer Duplo', 'Queijo', 0.060),
        ('Hambúrguer Duplo', 'Tomate', 0.040)
     ) as v(produto, insumo, quantidade)
join produto p on p.unidade_id = '00000000-0000-0000-0000-000000000001' and p.nome = v.produto
join ficha_tecnica f on f.unidade_id = p.unidade_id and f.produto_id = p.id
join insumo i on i.unidade_id = p.unidade_id and i.nome = v.insumo
on conflict (ficha_id, insumo_id) do nothing;
