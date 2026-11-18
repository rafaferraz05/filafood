-- Seed de desenvolvimento para F6 (Cardápio): preenche categorias/descrições e um período vigente
-- no Hambúrguer Clássico, mantendo compatibilidade total com V901 e V902.
update produto
set categoria = 'Hambúrgueres',
    descricao = 'Pão artesanal, carne 150g, queijo e tomate'
where id = '00000000-0000-0000-0000-000000000301';

update produto
set categoria = 'Hambúrgueres',
    descricao = 'Pão artesanal e dobro de carne (300g), queijo e tomate'
where id = '00000000-0000-0000-0000-000000000302';

update produto
set categoria = 'Acompanhamentos',
    descricao = 'Porção de batata rústica temperada com alecrim'
where id = '00000000-0000-0000-0000-000000000303';

insert into periodo_preco_produto (id, produto_id, preco, data_inicio, data_fim)
select '00000000-0000-0000-0000-000000000601', p.id, 27.90, current_date - 5, current_date + 25
from produto p
where p.id = '00000000-0000-0000-0000-000000000301'
  and not exists (
      select 1 from periodo_preco_produto pp where pp.produto_id = p.id
  );