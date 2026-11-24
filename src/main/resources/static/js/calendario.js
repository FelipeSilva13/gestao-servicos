/*
 * Calendário de agendamento.
 *
 *  <div data-calendario="seletor" data-url="/portal/agenda" data-nome="dataHora"
 *       data-ordem="12" data-valor="2026-12-04T10:00" data-obrigatorio></div>
 *    -> o cliente (ou a equipe) escolhe um dia destacado e depois um horário livre;
 *       o valor vai num <input type="hidden" name="dataHora"> dentro do próprio formulário.
 *
 *  <div data-calendario="gestor" data-url="/api/agenda" data-inicio="08:00" data-fim="18:00"></div>
 *    -> a empresa seleciona dias e abre/fecha a agenda deles.
 */
(function () {
  'use strict';

  var SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
  var SITUACAO = {
    DISPONIVEL: 'disponível',
    LOTADO: 'sem horários livres',
    FECHADO: 'fechado',
    PASSADO: 'indisponível'
  };

  // ---- utilitários -----------------------------------------------------------

  function el(tag, attrs, filhos) {
    var e = document.createElement(tag);
    Object.keys(attrs || {}).forEach(function (k) {
      if (k === 'class') e.className = attrs[k];
      else if (k === 'text') e.textContent = attrs[k];
      else if (k.indexOf('on') === 0) e.addEventListener(k.slice(2), attrs[k]);
      else if (attrs[k] === true) e.setAttribute(k, '');
      else if (attrs[k] !== false && attrs[k] != null) e.setAttribute(k, attrs[k]);
    });
    (filhos || []).forEach(function (f) { if (f) e.appendChild(typeof f === 'string' ? document.createTextNode(f) : f); });
    return e;
  }

  function pad(n) { return (n < 10 ? '0' : '') + n; }
  function chaveMes(ano, mes) { return ano + '-' + pad(mes); }
  function hojeMes() { var d = new Date(); return { ano: d.getFullYear(), mes: d.getMonth() + 1 }; }
  function somarMes(m, n) {
    var d = new Date(m.ano, m.mes - 1 + n, 1);
    return { ano: d.getFullYear(), mes: d.getMonth() + 1 };
  }
  function antesDe(a, b) { return a.ano < b.ano || (a.ano === b.ano && a.mes < b.mes); }
  function nomeMes(m) {
    var t = new Date(m.ano, m.mes - 1, 1).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
    return t.charAt(0).toUpperCase() + t.slice(1);
  }
  function partesData(iso) { var p = iso.split('-'); return { ano: +p[0], mes: +p[1], dia: +p[2] }; }
  function dataCurta(iso) { var p = partesData(iso); return pad(p.dia) + '/' + pad(p.mes); }
  function dataLonga(iso) {
    var p = partesData(iso);
    var t = new Date(p.ano, p.mes - 1, p.dia).toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });
    return t.charAt(0).toUpperCase() + t.slice(1);
  }
  function hojeIso() { var d = new Date(); return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()); }

  function buscar(url, opcoes) {
    return fetch(url, Object.assign({ credentials: 'same-origin', headers: { 'Accept': 'application/json' } }, opcoes || {}))
      .then(function (r) {
        if (r.status === 204) return null;
        return r.json().catch(function () { return {}; }).then(function (corpo) {
          if (!r.ok) throw new Error(corpo.erro || 'Não foi possível carregar a agenda (erro ' + r.status + ').');
          return corpo;
        });
      });
  }

  /** Cabeçalho com navegação entre meses. */
  function navegacao(mes, minimo, aoMudar) {
    var anterior = el('button', {
      type: 'button', class: 'cal-nav', 'aria-label': 'Mês anterior', text: '‹',
      disabled: minimo && !antesDe(minimo, mes),
      onclick: function () { aoMudar(somarMes(mes, -1)); }
    });
    var proximo = el('button', {
      type: 'button', class: 'cal-nav', 'aria-label': 'Próximo mês', text: '›',
      onclick: function () { aoMudar(somarMes(mes, 1)); }
    });
    return el('div', { class: 'cal-cabecalho' }, [anterior, el('strong', { class: 'cal-titulo', 'aria-live': 'polite', text: nomeMes(mes) }), proximo]);
  }

  /** Grade do mês: chama montarCelula(dia) para cada dia retornado pela API. */
  function grade(mes, dias, montarCelula) {
    var g = el('div', { class: 'cal-grade', role: 'grid' });
    SEMANA.forEach(function (s) { g.appendChild(el('span', { class: 'cal-semana', role: 'columnheader', text: s })); });
    var vazio = new Date(mes.ano, mes.mes - 1, 1).getDay();
    for (var i = 0; i < vazio; i++) g.appendChild(el('span', { class: 'cal-vazio', 'aria-hidden': 'true' }));
    var hoje = hojeIso();
    dias.forEach(function (d) {
      var c = montarCelula(d);
      if (d.data === hoje) c.classList.add('hoje');
      g.appendChild(c);
    });
    return g;
  }

  function legenda(itens) {
    return el('div', { class: 'cal-legenda' }, itens.map(function (i) {
      return el('span', {}, [el('i', { class: 'cal-ponto ' + i[0], 'aria-hidden': 'true' }), i[1]]);
    }));
  }

  // ---- seletor (cliente e detalhe da OS) ------------------------------------------

  function seletor(raiz) {
    var url = raiz.dataset.url;
    var ordem = raiz.dataset.ordem;
    var obrigatorio = raiz.hasAttribute('data-obrigatorio');
    var valorInicial = raiz.dataset.valor || '';
    var minimo = hojeMes();

    var campo = el('input', { type: 'hidden', name: raiz.dataset.nome || 'dataHora', value: valorInicial });
    var corpo = el('div', { class: 'cal-corpo' });
    var horarios = el('div', { class: 'cal-horarios', 'aria-live': 'polite' });
    var resumo = el('p', { class: 'cal-resumo' });
    raiz.classList.add('calendario');
    raiz.append(campo, corpo, horarios, resumo);

    var estado = {
      mes: valorInicial ? partesData(valorInicial.slice(0, 10)) : minimo,
      diaAberto: valorInicial ? valorInicial.slice(0, 10) : null,
      valor: valorInicial
    };
    if (antesDe(estado.mes, minimo)) estado.mes = minimo;

    function atualizarResumo() {
      if (estado.valor) {
        resumo.className = 'cal-resumo escolhido';
        resumo.textContent = 'Escolhido: ' + dataLonga(estado.valor.slice(0, 10)) + ' às ' + estado.valor.slice(11, 16);
      } else {
        resumo.className = 'cal-resumo';
        resumo.textContent = obrigatorio ? 'Escolha um dia destacado e depois um horário.' : 'Opcional: escolha um dia destacado e um horário.';
      }
    }

    function mostrarHorarios(dia) {
      horarios.replaceChildren();
      if (!dia) return;
      horarios.appendChild(el('p', { class: 'cal-horarios-titulo', text: 'Horários livres em ' + dataLonga(dia.data) }));
      var lista = el('div', { class: 'cal-horarios-lista', role: 'listbox', 'aria-label': 'Horários' });
      dia.horarios.forEach(function (h) {
        var valor = dia.data + 'T' + h;
        lista.appendChild(el('button', {
          type: 'button', class: 'cal-hora' + (estado.valor === valor ? ' selecionado' : ''), text: h,
          role: 'option', 'aria-selected': String(estado.valor === valor),
          onclick: function () {
            estado.valor = valor;
            campo.value = valor;
            raiz.classList.remove('cal-invalido');
            mostrarHorarios(dia);
            atualizarResumo();
          }
        }));
      });
      horarios.appendChild(lista);
    }

    function carregar() {
      corpo.replaceChildren(navegacao(estado.mes, minimo, function (m) { estado.mes = m; carregar(); }),
        el('p', { class: 'cal-carregando', text: 'Carregando agenda…' }));
      var q = url + '?mes=' + chaveMes(estado.mes.ano, estado.mes.mes) + (ordem ? '&ordemId=' + encodeURIComponent(ordem) : '');
      buscar(q).then(function (json) {
        estado.json = json;
        desenhar();
      }).catch(function (e) {
        corpo.replaceChildren(navegacao(estado.mes, minimo, function (m) { estado.mes = m; carregar(); }),
          el('p', { class: 'cal-erro', role: 'alert', text: e.message }));
      });
    }

    function desenhar() {
      var json = estado.json;
      var porData = {};
      json.dias.forEach(function (d) { porData[d.data] = d; });
      var disponiveis = json.dias.filter(function (d) { return d.situacao === 'DISPONIVEL'; }).length;

      var g = grade(estado.mes, json.dias, function (d) {
        var p = partesData(d.data);
        var livre = d.situacao === 'DISPONIVEL';
        var rotulo = p.dia + ' — ' + SITUACAO[d.situacao] + (livre ? ', ' + d.horarios.length + ' horário(s)' : '');
        return el('button', {
          type: 'button', role: 'gridcell', title: rotulo, 'aria-label': rotulo,
          class: 'cal-dia ' + d.situacao.toLowerCase() + (estado.diaAberto === d.data ? ' selecionado' : ''),
          disabled: !livre, 'aria-pressed': String(estado.diaAberto === d.data),
          onclick: function () { estado.diaAberto = d.data; desenhar(); }
        }, [el('span', { text: String(p.dia) })]);
      });

      corpo.replaceChildren(
        navegacao(estado.mes, minimo, function (m) { estado.mes = m; carregar(); }),
        g,
        legenda([['disponivel', 'Disponível'], ['lotado', 'Lotado'], ['fechado', 'Fechado']]),
        el('p', { class: 'hint', text: 'Os atendimentos têm intervalo mínimo de ' + json.intervaloHoras + ' horas entre si.' })
      );
      if (!disponiveis) {
        corpo.appendChild(el('p', { class: 'cal-aviso', text: 'Nenhum dia disponível em ' + nomeMes(estado.mes).toLowerCase() + '. Veja o próximo mês.' }));
      }
      mostrarHorarios(porData[estado.diaAberto] && porData[estado.diaAberto].situacao === 'DISPONIVEL' ? porData[estado.diaAberto] : null);
    }

    var form = raiz.closest('form');
    if (form) {
      form.addEventListener('submit', function (ev) {
        if (obrigatorio && !campo.value) {
          ev.preventDefault();
          raiz.classList.add('cal-invalido');
          resumo.className = 'cal-resumo erro';
          resumo.textContent = 'Escolha um dia e um horário antes de enviar.';
          raiz.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      });
    }

    atualizarResumo();
    carregar();
  }

  // ---- gestor (empresa abre/fecha dias) ----------------------------------------------

  function gestor(raiz) {
    var url = raiz.dataset.url;
    var minimo = hojeMes();
    var estado = { mes: minimo, selecionados: {}, foco: null, dias: [] };

    raiz.classList.add('calendario', 'cal-gestor');
    var inicio = el('input', { type: 'time', value: raiz.dataset.inicio || '08:00', step: '3600', 'aria-label': 'Abertura' });
    var fim = el('input', { type: 'time', value: raiz.dataset.fim || '18:00', step: '3600', 'aria-label': 'Fechamento' });
    var mensagem = el('div', { class: 'cal-mensagem', role: 'status', 'aria-live': 'polite' });
    var contador = el('span', { class: 'muted' });
    var btnAbrir = el('button', { type: 'button', class: 'btn', text: 'Abrir dias selecionados', onclick: abrir });
    var btnFechar = el('button', { type: 'button', class: 'btn btn-secondary', text: 'Fechar dias selecionados', onclick: fechar });

    var barra = el('div', { class: 'cal-barra' }, [
      el('div', { class: 'cal-janela' }, [
        el('label', {}, ['Abertura', inicio]),
        el('label', {}, ['Fechamento', fim])
      ]),
      el('div', { class: 'cal-acoes' }, [
        el('button', { type: 'button', class: 'btn btn-secondary', text: 'Selecionar dias úteis', onclick: selecionarUteis }),
        el('button', { type: 'button', class: 'btn btn-secondary', text: 'Limpar seleção', onclick: limpar }),
        btnAbrir, btnFechar, contador
      ])
    ]);
    var corpo = el('div', { class: 'cal-corpo' });
    var detalhe = el('aside', { class: 'cal-detalhe', 'aria-live': 'polite' });
    raiz.append(barra, mensagem, el('div', { class: 'cal-layout' }, [corpo, detalhe]));

    function avisar(texto, tipo) {
      mensagem.className = 'cal-mensagem ' + (tipo || '');
      mensagem.textContent = texto || '';
    }

    function selecionaveis() {
      return estado.dias.filter(function (d) { return d.situacao !== 'PASSADO'; });
    }

    function atualizarBarra() {
      var n = Object.keys(estado.selecionados).length;
      contador.textContent = n ? n + ' dia(s) selecionado(s)' : 'Clique nos dias para selecionar';
      btnAbrir.disabled = !n;
      btnFechar.disabled = !n;
    }

    function selecionarUteis() {
      selecionaveis().forEach(function (d) {
        var p = partesData(d.data);
        var dow = new Date(p.ano, p.mes - 1, p.dia).getDay();
        if (dow !== 0 && dow !== 6) estado.selecionados[d.data] = true;
      });
      desenhar();
    }

    function limpar() { estado.selecionados = {}; desenhar(); }

    function abrir() {
      var datas = Object.keys(estado.selecionados).sort();
      if (!inicio.value || !fim.value) { avisar('Informe o horário de abertura e de fechamento.', 'erro'); return; }
      avisar('Abrindo dias…');
      buscar(url + '/dias', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ datas: datas, inicio: inicio.value, fim: fim.value })
      }).then(function (r) {
        estado.selecionados = {};
        avisar(r.abertos + ' dia(s) aberto(s) das ' + inicio.value + ' às ' + fim.value + '. Os clientes já podem escolher esses dias.', 'sucesso');
        carregar();
      }).catch(function (e) { avisar(e.message, 'erro'); });
    }

    function fechar() {
      var datas = Object.keys(estado.selecionados).sort();
      var erros = [];
      var fechados = 0;
      avisar('Fechando dias…');
      datas.reduce(function (p, data) {
        return p.then(function () {
          return buscar(url + '/dias/' + data, { method: 'DELETE' })
            .then(function () { fechados++; delete estado.selecionados[data]; })
            .catch(function (e) { erros.push(e.message); });
        });
      }, Promise.resolve()).then(function () {
        if (erros.length) avisar((fechados ? fechados + ' dia(s) fechado(s). ' : '') + erros.join(' '), 'erro');
        else avisar(fechados + ' dia(s) fechado(s).', 'sucesso');
        carregar();
      });
    }

    function mostrarDetalhe() {
      detalhe.replaceChildren();
      var d = estado.dias.filter(function (x) { return x.data === estado.foco; })[0];
      if (!d) {
        detalhe.appendChild(el('p', { class: 'muted', text: 'Clique em um dia para ver os horários e os atendimentos marcados.' }));
        return;
      }
      detalhe.appendChild(el('h3', { text: dataLonga(d.data) }));
      detalhe.appendChild(el('p', { class: 'badge ' + ({ DISPONIVEL: 'success', LOTADO: 'warning', FECHADO: '', PASSADO: '' })[d.situacao],
        text: d.aberto ? (d.situacao === 'LOTADO' ? 'Aberto · lotado' : d.situacao === 'PASSADO' ? 'Encerrado' : 'Aberto') + ' · ' + d.inicio + '–' + d.fim : 'Fechado' }));

      detalhe.appendChild(el('p', { class: 'cal-horarios-titulo', text: 'Atendimentos marcados' }));
      if (!d.ocupacoes.length) {
        detalhe.appendChild(el('p', { class: 'muted', text: 'Nenhum atendimento neste dia.' }));
      } else {
        detalhe.appendChild(el('ul', { class: 'cal-ocupacoes' }, d.ocupacoes.map(function (o) {
          return el('li', {}, [
            el('strong', { text: o.hora }), ' ',
            el('a', { href: '/os/' + o.ordemId, text: o.numero }),
            ' · ' + (o.cliente || 'Sem cliente') + ' ',
            el('span', { class: 'badge ' + (o.confirmado ? 'success' : 'warning'), text: o.confirmado ? 'Confirmado' : 'A confirmar' })
          ]);
        })));
      }
      if (d.aberto && d.situacao !== 'PASSADO') {
        detalhe.appendChild(el('p', { class: 'cal-horarios-titulo', text: 'Horários ainda livres' }));
        detalhe.appendChild(d.horarios.length
          ? el('div', { class: 'cal-horarios-lista' }, d.horarios.map(function (h) { return el('span', { class: 'cal-hora estatico', text: h }); }))
          : el('p', { class: 'muted', text: 'Nenhum: os horários respeitam o intervalo de 4 horas entre atendimentos.' }));
      }
    }

    function desenhar() {
      var g = grade(estado.mes, estado.dias, function (d) {
        var p = partesData(d.data);
        var sel = !!estado.selecionados[d.data];
        var info = d.aberto ? d.inicio + '–' + d.fim : (d.situacao === 'PASSADO' ? '' : 'Fechado');
        var rotulo = p.dia + ' — ' + (d.aberto ? 'aberto ' + info : SITUACAO[d.situacao]) + (d.ocupacoes.length ? ', ' + d.ocupacoes.length + ' atendimento(s)' : '');
        return el('button', {
          type: 'button', role: 'gridcell', title: rotulo, 'aria-label': rotulo, 'aria-pressed': String(sel),
          class: 'cal-dia ' + (d.aberto ? 'aberto ' : '') + d.situacao.toLowerCase() + (sel ? ' marcado' : '') + (estado.foco === d.data ? ' foco' : ''),
          onclick: function () {
            estado.foco = d.data;
            if (d.situacao !== 'PASSADO') {
              if (estado.selecionados[d.data]) delete estado.selecionados[d.data];
              else estado.selecionados[d.data] = true;
            }
            desenhar();
          }
        }, [
          el('span', { class: 'cal-num', text: String(p.dia) }),
          info ? el('small', { text: info }) : null,
          d.ocupacoes.length ? el('em', { class: 'cal-qtd', text: d.ocupacoes.length + ' agend.' }) : null
        ]);
      });
      corpo.replaceChildren(
        navegacao(estado.mes, minimo, function (m) { estado.mes = m; carregar(); }),
        g,
        legenda([['disponivel', 'Aberto com horários'], ['lotado', 'Aberto e lotado'], ['fechado', 'Fechado'], ['marcado', 'Selecionado']])
      );
      atualizarBarra();
      mostrarDetalhe();
    }

    function carregar() {
      corpo.replaceChildren(navegacao(estado.mes, minimo, function (m) { estado.mes = m; carregar(); }),
        el('p', { class: 'cal-carregando', text: 'Carregando agenda…' }));
      buscar(url + '?mes=' + chaveMes(estado.mes.ano, estado.mes.mes)).then(function (json) {
        estado.dias = json.dias;
        desenhar();
      }).catch(function (e) { avisar(e.message, 'erro'); });
    }

    atualizarBarra();
    carregar();
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-calendario="seletor"]').forEach(seletor);
    document.querySelectorAll('[data-calendario="gestor"]').forEach(gestor);
  });
})();
