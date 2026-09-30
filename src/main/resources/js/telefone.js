const telefone = document.querySelector('input[name="telefone"]');

telefone.addEventListener('input', function () {

    let valor = this.value.replace(/\D/g, '');

    if (valor.length > 11) {
        valor = valor.substring(0, 11);
    }

    if (valor.length <= 10) {
        valor = valor.replace(
            /^(\d{2})(\d{0,4})(\d{0,4}).*/,
            '($1) $2-$3'
        );
    } else {
        valor = valor.replace(
            /^(\d{2})(\d{5})(\d{0,4}).*/,
            '($1) $2-$3'
        );
    }

    this.value = valor;
});