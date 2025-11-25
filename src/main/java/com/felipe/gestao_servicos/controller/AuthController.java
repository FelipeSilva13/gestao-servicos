package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.service.UsuarioService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    // Tela de login
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null)
            model.addAttribute("errorMessage", "Email ou senha inválidos");

        if (logout != null)
            model.addAttribute("logoutMessage", "Logout realizado");

        return "login";
    }

    // Tela de registro
    @GetMapping("/registro")
    public String registroPage() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @RequestParam @Email String email,
            @RequestParam @NotBlank String senha,
            Model model
    ) {
        try {
            service.registrar(email, senha);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "registro";
        }
    }
}
