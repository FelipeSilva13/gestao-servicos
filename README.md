# Sistema de Gestão de Serviços

Aplicação web para organizar a operação de empresas prestadoras de serviços técnicos. O projeto combina páginas renderizadas no servidor com uma API REST para clientes, técnicos, serviços e ordens de serviço.

## Funcionalidades

- Cadastro e consulta de clientes, técnicos e serviços.
- Criação e gestão de ordens de serviço, com status pendente, em execução e finalizada.
- Busca de ordens por status, técnico ou cliente; atualização de status registra datas de início e conclusão.
- Resumos de ordens e valores por técnico ou cliente.
- Registro de empresa com criação do primeiro usuário administrador e autenticação por formulário.
- Interface web com Thymeleaf e endpoints REST em `/api`.

### Portal do Cliente (`/portal`)

A empresa libera o acesso em **Clientes → cadeado** (login = e-mail do cliente). No portal, o cliente:

| Funcionalidade | Como funciona |
| --- | --- |
| Acompanhamento de serviços | Etapas e linha do tempo da OS; a página se atualiza sozinha quando algo muda. |
| Solicitação de atendimento | Abre uma OS com status `SOLICITADA`, que aparece para a equipe triar. |
| Aprovação de orçamento | A empresa envia o orçamento no detalhe da OS; o cliente aprova (`PENDENTE`) ou recusa (`RECUSADA`). |
| Histórico de serviços | Lista de todas as ordens do cliente, com filtros. |
| Envio de arquivos | Fotos, vídeos, PDF e documentos do Office (até 25 MB cada), salvos em `UPLOADS_DIR`. |
| Avaliação do atendimento | Após a conclusão, nota de 1 a 5 para o serviço e para o técnico; a média aparece em Técnicos. |
| Notificações | Sino com avisos a cada mudança de status, orçamento, agenda ou anexo (cliente e equipe). |
| Agendamento | Calendário interativo: o cliente só escolhe dias abertos pela empresa (em destaque) e horários livres; dias lotados ficam bloqueados e cada atendimento fica a pelo menos 4 horas do outro. A empresa confirma ou propõe outro horário. |

Na área da empresa, **Agenda** (`/agenda`) mostra o mês em calendário: selecione os dias, defina abertura e fechamento e clique em *Abrir dias selecionados*. Cada dia mostra os atendimentos marcados e os horários que ainda estão livres. Um dia com atendimento marcado só pode ser fechado depois que eles forem remarcados.

## Tecnologias

| Área | Tecnologias |
| --- | --- |
| Linguagem e build | Java 21, Maven Wrapper |
| Backend | Spring Boot 3.5, Spring Web, Spring Data JPA, Hibernate, Bean Validation |
| Interface | Thymeleaf, HTML, CSS e JavaScript |
| Persistência | PostgreSQL, Flyway |
| Segurança | Spring Security, BCrypt |
| Testes | JUnit 5, Mockito, Spring Boot Test |
| Container | Docker, Docker Compose |

## Arquitetura

O backend segue uma organização em camadas dentro de `com.felipe.gestao_servicos`:

```text
config/       Segurança e contexto de multitenancy
controller/   Endpoints que respondem dados (JSON da API, contadores, arquivos)
web/          Controllers que renderizam páginas Thymeleaf (inclusive login/registro)
service/      Regras de negócio e casos de uso
repository/   Persistência com Spring Data JPA
domain/       Entidades JPA
dto/          Contratos de entrada e resposta
enums/        Estados de domínio, como o status da ordem
```

Os controllers recebem requisições e delegam o processamento aos serviços. Os serviços concentram operações de negócio e usam repositories para consultar e persistir entidades. A API usa DTOs de entrada/saída em seus principais fluxos.

## Banco, segurança e multitenancy

- PostgreSQL é o banco configurado pela aplicação. O Hibernate está configurado para validar o schema na configuração padrão; o perfil local usa `ddl-auto=update`.
- Há sete migrations SQL versionadas com Flyway (`V1` a `V7`), incluindo a criação de tenants, a inclusão de `tenant_id` nas entidades de negócio, as tabelas do portal do cliente (`V6`) e os dias abertos da agenda (`V7`).
- Usuários do portal têm a role `ROLE_CLIENTE` e só acessam `/portal/**`, `/notificacoes/**` e os anexos das próprias ordens; as demais telas e a API são restritas à equipe (`ROLE_ADMIN`/`ROLE_USER`).
- O isolamento é por `tenant_id` no mesmo banco, não por schema ou database. O tenant associado ao usuário autenticado é colocado em um contexto por requisição; os serviços consultam registros usando esse tenant.
- O Spring Security configura login por formulário, BCrypt para senhas e autenticação obrigatória nas demais rotas. O cadastro público cria a empresa e seu administrador inicial.

## API e exemplos

As rotas da API exigem autenticação. Entre as operações disponíveis:

| Método | Rota | Operação |
| --- | --- | --- |
| `GET` | `/api/clientes` | Listar clientes do tenant atual |
| `POST` | `/api/clientes` | Criar cliente |
| `GET` | `/api/os` | Listar ordens de serviço |
| `PATCH` | `/api/os/{id}/status?status=EM_EXECUCAO` | Atualizar o status de uma ordem |
| `GET` | `/api/relatorios/tecnico/{id}` | Resumo de ordens por técnico |

Também existem endpoints REST para técnicos, serviços e operações de criação, edição, consulta e exclusão. A interface de login está em `/login` e o registro de empresa em `/registro`.

## Executar localmente

Pré-requisitos: Java 21, PostgreSQL em execução e um banco chamado `gestao_servicos`. A configuração padrão usa `localhost:5432`; informe as credenciais do banco pelas variáveis abaixo. O Flyway aplica as migrations na inicialização.

No PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/gestao_servicos"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "<senha-do-banco>"
.\mvnw.cmd spring-boot:run
```

O projeto também contém um `Dockerfile` multi-stage baseado em Eclipse Temurin 21 e uma definição Compose com PostgreSQL 13 e backend. No Compose atual, o Flyway está desativado; para um banco vazio, configure a inicialização do schema antes de usar essa opção.

Para executar os testes, configure as mesmas variáveis de conexão usadas pelo perfil de teste e rode:

```powershell
.\mvnw.cmd test
```

Os testes existentes cobrem o carregamento do contexto Spring e casos unitários de cadastro de empresa/administrador.

## 🌐 Deploy

<a href="https://gestao-servicos-0mw5.onrender.com/">https://gestao-servicos-0mw5.onrender.com</a>

