**Provisionamento seguro do banco de produção (MySQL)**

Resumo rápido
- Use um banco gerenciado (RDS, Cloud SQL, DigitalOcean, Aiven, PlanetScale, etc.) ou um servidor dedicado com TLS habilitado.
- Não rode o banco em container no ambiente de produção deste projeto; use o container apenas localmente (dev).

Passos básicos
1. Provisionar o serviço de banco (escolha um provedor gerenciado quando possível).
2. Criar um database separado `gestao_servicos` e um usuário dedicado (veja `db/provision_prod_db.sql`).
3. Habilitar TLS/SSL no servidor MySQL e obter certificado do provedor.
4. Restringir o acesso por IP ou VPC (whitelist) — permita apenas os servidores da sua aplicação ou da plataforma que vai rodar o serviço.
5. Habilitar backups automáticos e monitoramento.

Variáveis de ambiente necessárias
- `SPRING_DATASOURCE_URL` — exemplo: `jdbc:mysql://db.example.com:3306/gestao_servicos?useSSL=true&requireSSL=true&serverTimezone=UTC`
- `SPRING_DATASOURCE_USERNAME` — usuário criado para a aplicação
- `SPRING_DATASOURCE_PASSWORD` — senha do usuário
- `SPRING_PROFILES_ACTIVE=prod`

Configuração adicional de segurança
- Use contas de usuário com privilégios mínimos (apenas no database da aplicação).
- Não exponha credenciais em código-fonte ou repositório.
- Se possível, habilite autenticação por certificado ou IAM (oferecido por alguns provedores).

Considerações sobre Multi-Tenancy (cada empresa com suas "próprias tabelas")

Opções:
- A) Schema por tenant (recomendado se você quer tabelas separadas):
  - Cria um schema (esquema) por empresa: `tenant_123`.
  - A aplicação troca o schema ativo por tenant antes de executar operações (Hibernate multi-tenancy por SCHEMA).
  - Isolamento razoável; migrações podem ser aplicadas a todos os schemas com scripts ou ferramentas (Flyway/Liquibase).

- B) Tabelas por tenant (cada empresa tem cópias separadas das tabelas):
  - Mais complexo de manter; requer criação de tabelas dinamicamente por tenant.
  - Pode usar prefixos de tabela `tenant123_clientes` — geralmente desencorajado.

- C) Coluna `tenant_id` (mais simples):
  - Uma única cópia das tabelas com coluna `tenant_id` para isolar dados.
  - Escalável e mais fácil de migrar; exige aplicação para filtrar por `tenant_id` sempre.

Recomendação: usar **Schema por tenant** se quer tabelas separadas por empresa sem criar um database por cliente.

Migrações
- Use Flyway ou Liquibase. Para schema-per-tenant, crie uma rotina que aplique as migrations em cada schema nova.

Exemplo de criação de schema para um novo cliente (executar com usuário administrador):
```sql
CREATE SCHEMA IF NOT EXISTS tenant_123 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- então aplicar as migrations no schema tenant_123
```

Próximos passos que eu posso fazer para você:
- Implementar suporte a multi-tenancy (schema-per-tenant) no código Java com Hibernate.
- Adicionar integração com Flyway para gerenciar migrations por schema.
- Criar script de provisionamento automatizado para criação de tenant (schema + usuário opcional).
