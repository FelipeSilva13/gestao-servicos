-- Script de provisionamento para um banco MySQL de produção
-- Ajuste nomes/usuários/hosts conforme seu provedor

-- 1) Cria o banco de aplicação
CREATE DATABASE IF NOT EXISTS gestao_servicos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2) Cria usuário dedicado (substitua 'app_user' e 'secure_password')
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'secure_password';

-- 3) Concede permissões apenas ao banco da aplicação
GRANT ALL PRIVILEGES ON gestao_servicos.* TO 'app_user'@'%';
FLUSH PRIVILEGES;

-- NOTAS:
-- - Restrinja o host em vez de '%' quando souber os IPs/hosts do provedor.
-- - Habilite conexão TLS/SSL no servidor e exija TLS no client (ver README).
