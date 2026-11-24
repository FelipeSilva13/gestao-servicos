-- Área do técnico: acesso do técnico e resposta (aceite/recusa) às ordens atribuídas.

-- 1. Usuário de acesso vinculado a um técnico (liberado somente pelo administrador)
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS tecnico_id BIGINT;

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuario_tecnico
        FOREIGN KEY (tecnico_id) REFERENCES tecnicos(id) ON DELETE CASCADE;

-- 2. Resposta do técnico à ordem para a qual foi designado
ALTER TABLE ordens_servico
    ADD COLUMN IF NOT EXISTS tecnico_resposta      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS tecnico_respondido_em TIMESTAMP,
    ADD COLUMN IF NOT EXISTS tecnico_motivo_recusa VARCHAR(500);

-- Ordens já atribuídas antes desta funcionalidade são consideradas aceitas
UPDATE ordens_servico SET tecnico_resposta = 'ACEITA' WHERE tecnico_id IS NOT NULL AND tecnico_resposta IS NULL;
