package com.felipe.gestao_servicos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "forward:/empresa/index.html";
    }

    // Cadastro público: empresa + primeiro administrador.
    @GetMapping("/registro")
    public String registroPage() {
        return "forward:/empresa/index.html";
    }
}
