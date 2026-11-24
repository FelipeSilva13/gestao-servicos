// Campo telefone: aceita apenas dígitos (DDD + número), no máximo 11.
document.querySelectorAll('input[name="telefone"]').forEach(function (campo) {
    campo.addEventListener('input', function () {
        this.value = this.value.replace(/\D/g, '').substring(0, 11);
    });
});
