package me.dhanur.tasks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping({ "/", "/app", "/app/" })
    public String redirectToApp() {
        return "redirect:/app/index.html";
    }
}
