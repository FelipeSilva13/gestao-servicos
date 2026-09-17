ALTER TABLE clientes
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

UPDATE clientes
SET tenant_id = (SELECT id FROM tenants WHERE nome = 'Empresa Principal')
WHERE tenant_id IS NULL;

ALTER TABLE clientes
    ALTER COLUMN tenant_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_cliente_tenant'
          AND conrelid = 'clientes'::regclass
    ) THEN
        ALTER TABLE clientes
            ADD CONSTRAINT fk_cliente_tenant
                FOREIGN KEY (tenant_id)
                    REFERENCES tenants(id);
    END IF;
END
$$;
