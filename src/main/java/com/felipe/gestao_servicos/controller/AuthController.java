package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.dto.request.CadastroEmpresaRequest;
import com.felipe.gestao_servicos.service.CadastroEmpresaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final CadastroEmpresaService cadastroEmpresaService;

    public AuthController(CadastroEmpresaService cadastroEmpresaService) {
        this.cadastroEmpresaService = cadastroEmpresaService;
    }

    // Tela de login
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "success", required = false) String success,
                        Model model) {
        if (error != null)
            model.addAttribute("errorMessage", "Email ou senha inválidos");

        if (logout != null)
            model.addAttribute("logoutMessage", "Logout realizado");
        if (success != null)
            model.addAttribute("successMessage", success);

        return "usuario/login";
    }

    // Cadastro público: empresa + primeiro administrador.
    @GetMapping("/registro")
    public String registroPage() {
        return "usuario/registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @Valid CadastroEmpresaRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return "usuario/registro";
        }

        try {
                cadastroEmpresaService.cadastrar(request);
                return "redirect:/login?success=Usuário cadastrado com sucesso";

        } catch (Exception e) {

            model.addAttribute("errorMessage", e.getMessage());

            return "usuario/registro";
        }
    }
}
