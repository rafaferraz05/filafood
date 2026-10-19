import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';

const root = path.resolve(import.meta.dirname, '..');
const proto = path.join(root, 'docs/prototipos');
const walk = dir => fs.readdirSync(dir, {withFileTypes:true}).flatMap(e => e.isDirectory() ? walk(path.join(dir,e.name)) : [path.join(dir,e.name)]);
const htmls = walk(proto).filter(p => p.endsWith('.html'));
const novos = htmls.filter(p => p.includes(path.join('prototipos','atendimento') + path.sep));
const erros = [];
let marcadoresAntigos = 0, cenarios = 0;
for (const file of htmls) {
 const s = fs.readFileSync(file,'utf8');
 const novo = novos.includes(file);
 for (const m of s.matchAll(/(?:href|src)="([^"]+)"/g)) {
  let href = m[1];
  if (/^(https?:|mailto:|data:)/.test(href)) continue;
  if (href === '#') { if(novo) erros.push(file + ': ação sem destino'); else marcadoresAntigos++; continue; }
  const [p, fragment] = href.split('#');
  const target = p ? path.resolve(path.dirname(file),p.split('?')[0]) : file;
  if (!fs.existsSync(target)) erros.push(file + ': destino ausente ' + href);
  else if (fragment && target.endsWith('.html') && !fs.readFileSync(target,'utf8').includes('id="'+fragment+'"')) erros.push(file + ': âncora ausente ' + href);
 }
 if(!novo) continue;
 if (/<script\b/i.test(s)) erros.push(file + ': JavaScript fora do padrão');
 const ids = [...s.matchAll(/\bid="([^"]+)"/g)].map(m=>m[1]);
 if(new Set(ids).size !== ids.length) erros.push(file + ': id HTML repetido');
 for(const m of s.matchAll(/<label for="([^"]+)"/g)) if(!ids.includes(m[1])) erros.push(file + ': rótulo sem campo');
 const feature = s.match(/data-feature="([^"]+)"/)?.[1];
 const tag = s.match(/data-bdd="([^"]+)"/)?.[1];
 if(feature) {
  const f = path.join(root,'dominio-atendimento/src/test/resources/br/edu/cesar/filafood/dominio/atendimento',feature,feature+'.feature');
  if(!tag || !fs.readFileSync(f,'utf8').split(/\s+/).includes('@'+tag)) erros.push(file + ': cenário BDD não encontrado');
  else cenarios++;
 }
}
// Regressões semânticas dos fluxos corrigidos nas revisões independentes.
const ler = rel => fs.readFileSync(path.join(proto,'atendimento',rel),'utf8');
for (const nome of ['unidades-editada-pausada','unidades-editada-inativa','unidades-retomada-apos-edicao','unidades-reativada-apos-edicao']) {
 const s = ler('unidades/'+nome+'.html');
 if(!s.includes('<td>Boa Viagem Sul</td><td>Boa Viagem</td>')) erros.push(nome+': edição alterou a região indevidamente');
}
for (const [sufixo,rascunho] of [['duplo','dois-itens'],['editado','editado'],['somado','somado']]) {
 const s = ler('pedidos/novo-pedido-confirmar-descarte-'+sufixo+'.html');
 if(!s.includes('href="novo-pedido-'+rascunho+'.html">Voltar')) erros.push(sufixo+': desistência do descarte não preserva composição');
}
for (const [sufixo,id,quantidade,vegetariano] of [['tres','2402',3,false],['duplo','2403',2,true]]) {
 for(const nome of ['pedido-enviado','pedido-confirmar-cancelamento','pedido-cancelado']) {
  const s=ler('pedidos/'+nome+'-'+sufixo+'.html');
  if(!s.includes('Pedido #'+id)||!s.includes('<td>Clássico</td><td>'+quantidade+'</td>')||s.includes('<td>Vegetariano</td>')!==vegetariano) erros.push(nome+'-'+sufixo+': composição divergente');
 }
}
assert.equal(erros.length, 0, erros.join('\n'));
console.log(JSON.stringify({paginasGrupo:htmls.length,paginasRafael:novos.length,estadosComBDD:cenarios,linksSemDestinoNoLegado:marcadoresAntigos,erros:erros.length},null,2));
