package com.felipe.gestao_servicos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

public class HomeController {



    @Controller
    public class HomeController {

        @GetMapping("/")
        public String index() {
            return "index"; // Carrega templates/index.html
        }
    }

}
