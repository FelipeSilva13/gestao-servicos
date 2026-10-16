import { useEffect, useState } from 'react'
import { ArrowRight, ArrowUpRight, Eye, EyeOff, Layers3, LockKeyhole } from 'lucide-react'

export default function LoginPage() {
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const parametros = new URLSearchParams(window.location.search)
  const mensagem = parametros.has('error')
    ? { tipo: 'erro', texto: 'E-mail ou senha inválidos. Confira seus dados e tente novamente.' }
    : parametros.has('logout')
      ? { tipo: 'sucesso', texto: 'Você saiu da sua conta com segurança.' }
      : null

  useEffect(() => {
    document.title = 'Entrar | GestãoOS'
  }, [])

  return (
    <main className="registration-page">
      <section className="brand-panel" aria-label="GestãoOS">
        <a className="brand" href="/" aria-label="GestãoOS, início">
          <span className="brand-icon"><Layers3 size={21} strokeWidth={2.3} /></span>
          <span className="brand-name">GestãoOS<span>GESTÃO DE SERVIÇOS</span></span>
        </a>

        <div className="brand-copy">
          <span className="section-kicker"><span /> SUA EMPRESA, EM MOVIMENTO</span>
          <h1>Sua operação segue em frente. <em>Entre.</em></h1>
          <p>Acesse seu espaço de trabalho e acompanhe os serviços da sua empresa em um só lugar.</p>
        </div>

        <div className="operation-preview" aria-hidden="true">
          <div className="preview-topline">
            <span><i /> PAINEL DA OPERAÇÃO</span>
            <span>VISÃO GERAL</span>
          </div>
          <div className="preview-title">Ordens de serviço <span>AGOSTO 2026</span></div>
          <div className="preview-row">
            <span className="preview-number">OS-024</span><span>Instalação elétrica</span><b className="preview-open">Aberta</b>
          </div>
          <div className="preview-row">
            <span className="preview-number">OS-023</span><span>Reparo hidráulico</span><b className="preview-progress">Em andamento</b>
          </div>
          <div className="preview-row">
            <span className="preview-number">OS-022</span><span>Manutenção preventiva</span><b className="preview-done">Concluída</b>
          </div>
          <div className="preview-footer"><span>ATUALIZADO AGORA</span><span>03 REGISTROS <ArrowUpRight size={13} /></span></div>
        </div>

        <div className="brand-footer"><span>GESTÃO QUE ACOMPANHA O SEU RITMO</span><span>02 / 02</span></div>
      </section>

      <section className="signup-section">
        <div className="signup-content">
          <div className="signup-topline">
            <span>AINDA NÃO TEM UMA CONTA?</span>
            <a href="/registro">Criar empresa <ArrowUpRight size={15} /></a>
          </div>

          <header className="form-heading">
            <span className="step-label"><span>02</span> ACESSO À CONTA</span>
            <h2>Bem-vindo de volta.</h2>
            <p>Entre com o e-mail e a senha cadastrados para sua empresa.</p>
          </header>

          <form className="signup-form" action="/do-login" method="post">
            <label htmlFor="email">E-mail</label>
            <input
              id="email"
              name="username"
              type="email"
              placeholder="voce@empresa.com.br"
              autoComplete="username"
              maxLength={255}
              required
            />

            <label htmlFor="senha">Senha</label>
            <div className="password-field">
              <LockKeyhole size={17} aria-hidden="true" />
              <input
                id="senha"
                name="password"
                type={mostrarSenha ? 'text' : 'password'}
                placeholder="Sua senha"
                autoComplete="current-password"
                required
              />
              <button
                className="password-toggle"
                type="button"
                onClick={() => setMostrarSenha(!mostrarSenha)}
                aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
                title={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {mostrarSenha ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>

            {mensagem && (
              <div className={`form-feedback ${mensagem.tipo}`} role={mensagem.tipo === 'erro' ? 'alert' : 'status'}>
                <p>{mensagem.texto}</p>
              </div>
            )}

            <button className="submit-button" type="submit">
              Entrar na minha conta
              <ArrowRight size={18} />
            </button>

            <p className="privacy-note">Seus dados de acesso são protegidos e vinculados à sua empresa.</p>
          </form>

          <footer className="signup-footer">
            <span>© 2026 GestãoOS</span>
            <span>FEITO PARA QUEM FAZ ACONTECER</span>
          </footer>
        </div>
      </section>
    </main>
  )
}
