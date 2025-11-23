package org.openpodcastapi.opa.controllers;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class DocsController {

    @GetMapping("/docs")
    public String docs() {
        return "forward:/docs/index.html";
    }

    @GetMapping("/docs/")
    public String docsWithSlash() {
        return "forward:/docs/index.html";
    }
}