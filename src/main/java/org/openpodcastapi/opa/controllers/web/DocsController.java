package org.openpodcastapi.opa.controllers.web;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class DocsController {

    // === Docs index page ===
    @GetMapping("/docs")
    public String docs() {
        return "forward:/docs/index.html";
    }

    // === Docs page with trailing slash ===
    @GetMapping("/docs/")
    public String docsWithSlash() {
        return "forward:/docs/index.html";
    }
}