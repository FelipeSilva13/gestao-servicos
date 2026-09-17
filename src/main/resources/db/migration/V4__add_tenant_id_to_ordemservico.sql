ALTER TABLE ordens_servico
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

UPDATE ordens_servico
SET tenant_id = (SELECT id FROM tenants WHERE nome = 'Empresa Principal')
WHERE tenant_id IS NULL;

ALTER TABLE ordens_servico
    ALTER COLUMN tenant_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_ordens_servico_tenant'
          AND conrelid = 'ordens_servico'::regclass
    ) THEN
        ALTER TABLE ordens_servico
            ADD CONSTRAINT fk_ordens_servico_tenant
                FOREIGN KEY (tenant_id)
                    REFERENCES tenants(id);
    END IF;
END
$$;
