package com.fpoly.shared_learning_materials.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            model.addAttribute("username", auth.getName());
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
        }

        return "client/index";
    }

    @GetMapping("/home")
    public String homeAlias(Model model) {
        return home(model);
    }

    @GetMapping("/test")
    public String testPages(Model model) {
        model.addAttribute("pageTitle", "Test All Client Pages");
        return "test/client-pages";
    }
}