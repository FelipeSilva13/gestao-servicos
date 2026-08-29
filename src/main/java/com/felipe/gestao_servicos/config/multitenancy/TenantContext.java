package com.felipe.gestao_servicos.config.multitenancy;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenant(String tenant) {
        CURRENT_TENANT.set(tenant);
    }

    public static String getCurrentTenant() {
        String t = CURRENT_TENANT.get();
        return t != null ? t : "default";
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
