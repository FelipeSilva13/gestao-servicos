import { useState } from 'react'
import type { FormEvent } from 'react'
import { ArrowRight, ArrowUpRight, Eye, EyeOff, Layers3, LockKeyhole } from 'lucide-react'

type CadastroEmpresaResponse = {
  id: number
  nomeEmpresa: string
  emailAdministrador: string
}

type ApiError = {
  detail?: string
  message?: string
}

export default function RegistrationPage() {
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [feedback, setFeedback] = useState<{ tipo: 'erro' | 'sucesso'; mensagem: string } | null>(null)

  async function cadastrar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const formElement = event.currentTarget
    setEnviando(true)
    setFeedback(null)

    const form = new FormData(formElement)
    const request = {
      nomeEmpresa: String(form.get('nomeEmpresa') ?? '').trim(),
      email: String(form.get('email') ?? '').trim(),
      senha: String(form.get('senha') ?? ''),
    }

    try {
      const response = await fetch('/api/empresas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
      })
      const result = await response.json().catch(() => null) as CadastroEmpresaResponse | ApiError | null

      if (!response.ok) {
        const error = result && 'detail' in result ? result.detail : result && 'message' in result ? result.message : null
        throw new Error(error || 'Não foi possível concluir o cadastro. Confira os dados e tente novamente.')
      }

      const cadastro = result as CadastroEmpresaResponse
      setFeedback({
        tipo: 'sucesso',
        mensagem: `${cadastro.nomeEmpresa} foi cadastrada. Agora você já pode entrar com ${cadastro.emailAdministrador}.`,
      })
      formElement.reset()
    } catch (error) {
      setFeedback({
        tipo: 'erro',
        mensagem: error instanceof Error ? error.message : 'Ocorreu um erro ao criar a empresa.',
      })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className="registration-page">
      <section className="brand-panel" aria-label="GestãoOS">
        <a className="brand" href="/" aria-label="GestãoOS, início">
          <span className="brand-icon"><Layers3 size={21} strokeWidth={2.3} /></span>
          <span className="brand-name">GestãoOS<span>GESTÃO DE SERVIÇOS</span></span>
        </a>

        <div className="brand-copy">
          <span className="section-kicker"><span /> SUA EMPRESA, EM MOVIMENTO</span>
          <h1>Uma operação mais clara começa com um novo <em>começo.</em></h1>
          <p>Crie o espaço da sua empresa e dê o primeiro acesso a quem cuida da operação.</p>
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

        <div className="brand-footer"><span>GESTÃO QUE ACOMPANHA O SEU RITMO</span><span>01 / 02</span></div>
      </section>

      <section className="signup-section">
        <div className="signup-content">
          <div className="signup-topline">
            <span>JÁ TEM UMA CONTA?</span>
            <a href="/login">Entrar <ArrowUpRight size={15} /></a>
          </div>

          <header className="form-heading">
            <span className="step-label"><span>01</span> CADASTRO DA EMPRESA</span>
            <h2>Comece por aqui.</h2>
            <p>Preencha os dados para criar sua empresa e o acesso administrador.</p>
          </header>

          <form className="signup-form" onSubmit={cadastrar}>
            <label htmlFor="nomeEmpresa">Nome da empresa</label>
            <input
              id="nomeEmpresa"
              name="nomeEmpresa"
              type="text"
              placeholder="Ex.: Horizonte Serviços"
              autoComplete="organization"
              maxLength={255}
              required
            />

            <label htmlFor="email">E-mail do administrador</label>
            <input
              id="email"
              name="email"
              type="email"
              placeholder="voce@empresa.com.br"
              autoComplete="email"
              maxLength={255}
              required
            />

            <label htmlFor="senha">Crie uma senha</label>
            <div className="password-field">
              <LockKeyhole size={17} aria-hidden="true" />
              <input
                id="senha"
                name="senha"
                type={mostrarSenha ? 'text' : 'password'}
                placeholder="Pelo menos 6 caracteres"
                autoComplete="new-password"
                minLength={6}
                maxLength={100}
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

            {feedback && (
              <div className={`form-feedback ${feedback.tipo}`} role={feedback.tipo === 'erro' ? 'alert' : 'status'}>
                <p>{feedback.mensagem}</p>
                {feedback.tipo === 'sucesso' && <a href="/login">Ir para o login <ArrowRight size={15} /></a>}
              </div>
            )}

            <button className="submit-button" type="submit" disabled={enviando}>
              {enviando ? 'Criando sua empresa...' : 'Criar empresa'}
              {!enviando && <ArrowRight size={18} />}
            </button>

            <p className="privacy-note">Seus dados são usados apenas para configurar o acesso da sua empresa.</p>
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