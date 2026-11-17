// Atualizações em tempo real por consulta periódica (polling leve).
(function () {
  'use strict';

  var INTERVALO_NOTIFICACOES = 30000;
  var INTERVALO_ORDEM = 15000;

  function buscarJson(url) {
    return fetch(url, { credentials: 'same-origin', headers: { Accept: 'application/json' } })
      .then(function (r) { return r.ok ? r.json() : null; })
      .catch(function () { return null; });
  }

  // Contador do sino de notificações
  var contadores = document.querySelectorAll('[data-notificacoes-contador]');
  if (contadores.length) {
    setInterval(function () {
      if (document.hidden) return;
      buscarJson('/notificacoes/contagem').then(function (dados) {
        if (!dados) return;
        contadores.forEach(function (el) {
          el.textContent = dados.naoLidas > 99 ? '99+' : String(dados.naoLidas);
          el.hidden = dados.naoLidas === 0;
        });
      });
    }, INTERVALO_NOTIFICACOES);
  }

  // Página de acompanhamento: recarrega quando a ordem muda
  var ordem = document.querySelector('[data-acompanhar-url]');
  if (ordem) {
    var versaoAtual = ordem.getAttribute('data-versao');
    var aviso = document.querySelector('[data-aviso-atualizacao]');
    setInterval(function () {
      if (document.hidden) return;
      buscarJson(ordem.getAttribute('data-acompanhar-url')).then(function (dados) {
        if (!dados || dados.versao === versaoAtual) return;
        // Não interrompe quem está preenchendo um formulário
        var digitando = document.activeElement && /INPUT|TEXTAREA|SELECT/.test(document.activeElement.tagName);
        if (digitando && aviso) {
          aviso.hidden = false;
        } else {
          window.location.reload();
        }
      });
    }, INTERVALO_ORDEM);
  }
})();
