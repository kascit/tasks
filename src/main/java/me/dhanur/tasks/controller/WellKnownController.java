package me.dhanur.tasks.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WellKnownController {

    @GetMapping(value = "/.well-known/web-app-origin-association", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> webAppOriginAssociation() {
        return Map.of(
                "web_apps",
                List.of(
                        Map.of(
                                "manifest",
                                "https://dhanur.me/icons/site.webmanifest",
                                "details",
                                Map.of("paths", List.of("/*")))));
    }
}
