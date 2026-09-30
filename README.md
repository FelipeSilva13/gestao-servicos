# Sistema de Gestão de Serviços

Aplicação web para organizar a operação de empresas prestadoras de serviços técnicos. O projeto combina páginas renderizadas no servidor com uma API REST para clientes, técnicos, serviços e ordens de serviço.

## Funcionalidades

- Cadastro e consulta de clientes, técnicos e serviços.
- Criação e gestão de ordens de serviço, com status pendente, em execução e finalizada.
- Busca de ordens por status, técnico ou cliente; atualização de status registra datas de início e conclusão.
- Resumos de ordens e valores por técnico ou cliente.
- Registro de empresa com criação do primeiro usuário administrador e autenticação por formulário.
- Interface web com Thymeleaf e endpoints REST em `/api`.

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
controller/   Endpoints REST e autenticação
web/          Controllers das páginas Thymeleaf
service/      Regras de negócio e casos de uso
repository/   Persistência com Spring Data JPA
domain/       Entidades JPA
dto/          Contratos de entrada e resposta
enums/        Estados de domínio, como o status da ordem
```

Os controllers recebem requisições e delegam o processamento aos serviços. Os serviços concentram operações de negócio e usam repositories para consultar e persistir entidades. A API usa DTOs de entrada/saída em seus principais fluxos.

## Banco, segurança e multitenancy

- PostgreSQL é o banco configurado pela aplicação. O Hibernate está configurado para validar o schema na configuração padrão; o perfil local usa `ddl-auto=update`.
- Há quatro migrations SQL versionadas com Flyway (`V1` a `V4`), incluindo a criação de tenants e a inclusão de `tenant_id` nas entidades de negócio.
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

