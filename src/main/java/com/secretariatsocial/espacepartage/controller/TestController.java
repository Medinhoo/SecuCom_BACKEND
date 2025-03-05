package com.secretariatsocial.espacepartage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/public")
    public String publicAccess() {
        return "Contenu public accessible à tous";
    }

    @GetMapping("/private")
    public String privateAccess() {
        return "Contenu privé accessible uniquement aux utilisateurs authentifiés";
    }
}